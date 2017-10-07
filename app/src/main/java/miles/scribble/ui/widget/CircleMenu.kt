package miles.scribble.ui.widget

import android.animation.*
import android.annotation.SuppressLint
import android.content.Context
import android.os.Parcel
import android.os.Parcelable
import android.support.design.widget.CoordinatorLayout
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.Snackbar
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnticipateInterpolator
import android.view.animation.DecelerateInterpolator
import android.view.animation.OvershootInterpolator
import io.reactivex.disposables.Disposable
import miles.redux.core.Dispatcher
import miles.redux.rx.flowable
import miles.scribble.R
import miles.scribble.home.events.CircleMenuEvents
import miles.scribble.home.viewmodel.HomeViewModel
import miles.scribble.util.Circle
import miles.scribble.util.ViewUtils
import miles.scribble.util.extensions.*
import java.util.*

/**
 * Created by milespeele on 8/7/15.
 */
class CircleMenu : ViewGroup {

    lateinit var flowableDisposable : Disposable

    lateinit var viewModel : HomeViewModel
    lateinit var dispatcher : Dispatcher<CircleMenuEvents, CircleMenuEvents>

    internal val toggle by lazyInflate<FloatingActionButton>(R.id.menu_toggle)
    internal val eraser by lazyInflate<FloatingActionButton>(R.id.menu_erase)
    internal val saver by lazyInflate<FloatingActionButton>(R.id.menu_upload)

    private lateinit var circle: Circle
    private var clickedItem: FloatingActionButton? = null
    private val itemPositions: ArrayList<ItemPosition> = ArrayList()
    private val gestureDetector: GestureDetector = GestureDetector(context, GestureListener())

    private var isMenuShowing = false
    private var isAnimating = false
    private var isDragging = false
    private var isFlinging = false
    private var lastAngle: Double = Math.toRadians(0.0)

    val isVisible: Boolean
        get() = isMenuShowing && visibility == View.VISIBLE

    val cx: Float
        get() = circle.cx

    val cy: Float
        get() = circle.cy

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init()
    }

    private fun init() {
        setWillNotDraw(false)
        descendantFocusability = ViewGroup.FOCUS_AFTER_DESCENDANTS
    }

    override fun onFinishInflate() {
        super.onFinishInflate()

        flowableDisposable = flowable(viewModel.store)
                .subscribe {
                    if (it.isMenuOpen) {
                        show()
                    } else {
                        hide()
                    }
                }

        toggle.setOnClickListener {
            toggleMenu()
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()

        flowableDisposable.dispose()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        measureChildWithMargins(toggle, widthMeasureSpec, 0, heightMeasureSpec, 0)

        (0..childCount - 1)
                .map { getChildAt(it) }
                .filter { it !== toggle }
                .forEach { measureChildWithMargins(it, widthMeasureSpec, 0, heightMeasureSpec, 0) }

        val width = View.getDefaultSize(suggestedMinimumWidth, widthMeasureSpec)

        setMeasuredDimension(width, width shr 1)
    }

    override fun generateDefaultLayoutParams(): LayoutParams {
        return MarginLayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
    }

    override fun generateLayoutParams(p: LayoutParams): LayoutParams {
        return MarginLayoutParams(p)
    }

    override fun generateLayoutParams(attrs: AttributeSet): LayoutParams {
        return MarginLayoutParams(context, attrs)
    }

    @SuppressLint("DrawAllocation")
    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        if (itemPositions.isNotEmpty()) {
            return
        }

        val lps = toggle.layoutParams as MarginLayoutParams

        toggle.layout(r / 2 - toggle.measuredWidth / 2,
                measuredHeight - toggle.measuredHeight - lps.bottomMargin,
                r / 2 + toggle.measuredWidth / 2,
                measuredHeight - lps.bottomMargin)

        circle = Circle(ViewUtils.relativeCenterX(toggle), ViewUtils.relativeCenterY(toggle),
                toggle.measuredHeight * 3.75f)
        itemPositions.add(ItemPosition(toggle, cx, cy, toggle.radius()))

        val mItemRadius = (toggle.measuredHeight * 3).toFloat()
        val count = childCount
        val slice = Math.toRadians(360.0 / (count - 1))

        for (i in 0 until count) {
            val child = getChildAt(i) as FloatingActionButton
            if (child.id != R.id.menu_toggle) {
                val angle = i * slice
                val x = cx + mItemRadius * Math.cos(angle)
                val y = cy - mItemRadius * Math.sin(angle)

                child.layout(x.toInt() - child.measuredWidth / 2,
                        y.toInt() - child.measuredHeight / 2,
                        x.toInt() + child.measuredWidth / 2,
                        y.toInt() + child.measuredHeight / 2)

                val position = ItemPosition(child, x.toFloat(), y.toFloat(), child.radius())
                itemPositions.add(position)

                child.x -= position.itemCircle.cx - cx
                child.y -=  position.itemCircle.cy - cy
                if (isMenuShowing) {
                    child.alpha = 1f
                    position.update(lastAngle)
                } else {
                    child.alpha = 0f
                }
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y

        if (!circle.contains(x, y)) {
            return false
        }

        if (!isEnabled) {
            return false
        }

        if (isAnimating) {
            return false
        }

        if (!isVisible) {
            parent.requestDisallowInterceptTouchEvent(true)
            val v = getClickedItem(x, y)
            if (v != null && v.id == R.id.menu_toggle) {
                v.performClick()
                toggleMenu()
            }
            return false
        }

        gestureDetector.onTouchEvent(event)

        when (event.action and MotionEvent.ACTION_MASK) {
            MotionEvent.ACTION_DOWN -> {
                clickedItem = getClickedItem(x, y)

                if (isFlinging) {
                    isFlinging = false
                }

                lastAngle = circle.angleInDegrees(x, y)
            }
            MotionEvent.ACTION_MOVE -> {
                val degrees = circle.angleInDegrees(x, y)
                val rotater = degrees - lastAngle

                if (isDragging) {
                    updateItemPositions(rotater)
                }

                lastAngle = degrees

                isDragging = true
            }
            MotionEvent.ACTION_UP -> {
                isDragging = false
                clickedItem?.takeIf { it == getClickedItem(x, y) }?.let {
                            dispatchClickEvent(it)
                            clickedItem = null
                        }
            }
        }

        return true
    }

    private fun toggleMenu() {
        if (!isAnimating) {
            dispatcher.dispatch(CircleMenuEvents.ToggleClicked(!isMenuShowing))

            if (isMenuShowing) {
                hide()
            } else {
                show()
            }
        }
    }

    private fun rotateToggleOpen() {
        ObjectAnimator.ofFloat(toggle, View.ROTATION,
                toggle.rotation, toggle.rotation - 135f).start()
    }

    private fun rotateToggleClosed() {
        ObjectAnimator.ofFloat(toggle, View.ROTATION,
                toggle.rotation, toggle.rotation - 135f)
                .setDuration((HIDE_DIFF + DURATION + DELAY_INCREMENT * 10).toLong())
                .start()
    }

    private fun updateItemPositions(rotater: Double) {
        for (itemPosition in itemPositions) {
            itemPosition.update(rotater)
        }
    }

    private fun getClickedItem(x: Float, y: Float): FloatingActionButton? {
        return itemPositions.firstOrNull { it.contains(x, y) }?.fab
    }

    private fun dispatchClickEvent(view: View) {
        view.performClick()

        when (view.id) {
            R.id.menu_redo -> {
                if (viewModel.state.history.canRedo) {
                    dispatcher.dispatch(CircleMenuEvents.RedrawStarted(false))

                    dispatcher.dispatch(CircleMenuEvents.Redraw())

                    dispatcher.dispatch(CircleMenuEvents.RedrawCompleted())
                } else {
                    Snackbar.make(parent as CoordinatorLayout, R.string.snackbar_no_more_redo, Snackbar.LENGTH_SHORT)
                            .adjustImmersiveHeight()
                            .show()
                }
            }
            R.id.menu_undo -> {
                if (viewModel.state.history.canUndo) {
                    dispatcher.dispatch(CircleMenuEvents.RedrawStarted(true))

                    dispatcher.dispatch(CircleMenuEvents.Redraw())

                    dispatcher.dispatch(CircleMenuEvents.RedrawCompleted())
                } else {
                    Snackbar.make(parent as CoordinatorLayout, R.string.snackbar_no_more_undo, Snackbar.LENGTH_SHORT)
                            .adjustImmersiveHeight()
                            .show()
                }
            }
            R.id.menu_stroke_color -> {
                dispatcher.dispatch(CircleMenuEvents.StrokeColorClicked())
            }
            R.id.menu_canvas_color -> {
                dispatcher.dispatch(CircleMenuEvents.BackgroundColorClicked())
            }
            R.id.menu_erase -> {
                if (eraser.isSelected) {
                    eraser.scaleDown(1f, 1f)
                } else {
                    eraser.scaleUp(1.3f, 1.3f)
                }.start()

                eraser.isSelected = !eraser.isSelected
                dispatcher.dispatch(CircleMenuEvents.EraserClicked(eraser.isSelected))
            }
            R.id.menu_ink -> {
                dispatcher.dispatch(CircleMenuEvents.InkClicked())
            }
            R.id.menu_brush -> {
                dispatcher.dispatch(CircleMenuEvents.BrushClicked())
            }
            R.id.menu_image -> {
                dispatcher.dispatch(CircleMenuEvents.PictureClicked())
            }
        }
    }

    private fun show() {
        if (!isMenuShowing && !isAnimating) {
            rotateToggleOpen()

            val anims = ArrayList<Animator>()

            val max = Collections.max(itemPositions, ItemPositionComparator())
            val ndxOfMax = itemPositions.indexOf(max)
            var delay = INITIAL_DELAY
            for (i in itemPositions.indices) {
                var sum = i + ndxOfMax
                if (sum > itemPositions.size - 1) {
                    sum -= itemPositions.size
                }

                val position = itemPositions[sum]
                val view = position.fab

                if (view.id == R.id.menu_toggle) {
                    continue
                }

                val diffX = position.itemCircle.cx - cx
                val diffY = position.itemCircle.cy - cy

                val out = ObjectAnimator.ofPropertyValuesHolder(view,
                        PropertyValuesHolder.ofFloat(View.X, view.x + diffX),
                        PropertyValuesHolder.ofFloat(View.Y, view.y + diffY),
                        PropertyValuesHolder.ofFloat(View.ALPHA, 0.0f, 1.0f))
                out.startDelay = delay.toLong()
                out.addListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationStart(animation: Animator) {
                        view.visibility = View.VISIBLE
                    }
                })
                out.duration = DURATION.toLong()
                out.interpolator = OVERSHOOT_INTERPOLATOR

                val rotate = ObjectAnimator.ofFloat(view,
                        View.ROTATION, 0f, 360f)
                rotate.interpolator = DecelerateInterpolator()
                rotate.duration = DURATION.toLong()
                rotate.startDelay = delay.toLong()

                delay += DELAY_INCREMENT

                anims.add(out)
                anims.add(rotate)
            }

            val set = AnimatorSet()
            set.playTogether(anims)
            set.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationStart(animation: Animator) {
                    isAnimating = true
                    isMenuShowing = true
                }

                override fun onAnimationEnd(animation: Animator) {
                    isAnimating = false
                }
            })
            set.start()
        }
    }

    private fun hide() {
        if (isMenuShowing && !isAnimating) {
            rotateToggleClosed()

            val anims = ArrayList<Animator>()

            val max = Collections.max(itemPositions, ItemPositionComparator())
            val ndxOfMax = itemPositions.indexOf(max)
            var delay = INITIAL_DELAY
            for (i in itemPositions.indices) {
                var sum = i + ndxOfMax
                if (sum > itemPositions.size - 1) {
                    sum -= itemPositions.size
                }

                val position = itemPositions[sum]
                val view = position.fab

                if (view.id == R.id.menu_toggle) {
                    continue
                }

                val diffX = position.itemCircle.cx - cx
                val diffY = position.itemCircle.cy - cy

                val out = ObjectAnimator.ofPropertyValuesHolder(view,
                        PropertyValuesHolder.ofFloat(View.X, view.x - diffX),
                        PropertyValuesHolder.ofFloat(View.Y, view.y - diffY),
                        PropertyValuesHolder.ofFloat(View.ALPHA, 1.0f, 0.0f))
                out.startDelay = delay.toLong()
                out.addListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        view.visibility = View.GONE
                    }
                })
                out.startDelay = delay.toLong()
                out.duration = DURATION.toLong()
                out.interpolator = ANTICIPATE_INTERPOLATOR

                val rotate = ObjectAnimator.ofFloat(view,
                        View.ROTATION, 0f, 360f)
                rotate.interpolator = DecelerateInterpolator()
                rotate.duration = DURATION.toLong()
                rotate.startDelay = delay.toLong()

                delay += DELAY_INCREMENT

                anims.add(out)
                anims.add(rotate)
            }

            val set = AnimatorSet()
            set.playTogether(anims)
            set.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationStart(animation: Animator) {
                    isAnimating = true
                }

                override fun onAnimationEnd(animation: Animator) {
                    isMenuShowing = false
                    isAnimating = false
                }
            })
            set.start()
        }
    }

    private inner class ItemPosition(val fab: FloatingActionButton, itemX: Float, itemY: Float, radius: Float) {

        val itemCircle: Circle = Circle(itemX, itemY, radius)

        fun update(matrixAngle: Double) {
            val angleInRads = Math.toRadians(matrixAngle)

            val cosAngle = Math.cos(angleInRads)
            val sinAngle = Math.sin(angleInRads)

            val dx = itemCircle.cx - cx
            val dy = itemCircle.cy - cy

            var rx = (dx * cosAngle - dy * sinAngle).toFloat()
            var ry = (dx * sinAngle + dy * cosAngle).toFloat()

            rx += cx
            ry += cy

            itemCircle.cx = rx
            itemCircle.cy = ry

            val radius = itemCircle.radius

            fab.x = rx - radius
            fab.y = ry - radius
        }

        fun contains(x: Float, y: Float): Boolean {
            return itemCircle.contains(x, y)
        }
    }

    private inner class ItemPositionComparator : Comparator<ItemPosition> {

        override fun compare(lhs: ItemPosition, rhs: ItemPosition): Int {
            val left = lhs.itemCircle
            val right = rhs.itemCircle
            if (left.cx < right.cx) return -1
            if (left.cx > right.cx) return 1
            return 0
        }
    }

    private inner class GestureListener : GestureDetector.SimpleOnGestureListener() {

        override fun onFling(e1: MotionEvent, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
            if (isVisible && isMinXDist(e1, e2)) {
                isDragging = false

                isFlinging = true

                val angle = circle.angleInDegrees(e2.x - e1.x, e2.y - e1.y)
                val velocity = velocityX / 10 + velocityY / 10
                post(FlingRunnable(velocity, angle <= 45.0))

                return true
            }

            return false
        }

        private fun isMinXDist(e1: MotionEvent, e2: MotionEvent): Boolean {
            return Math.abs(e1.x - e2.x) > 50
        }
    }

    private inner class FlingRunnable(private var velocity: Float, private val isRtL: Boolean) : Runnable {

        override fun run() {
            if (Math.abs(velocity) > 5 && isFlinging) {
                if (isRtL && velocity > 0) {
                    updateItemPositions((-velocity / 75).toDouble())
                } else {
                    updateItemPositions((velocity / 75).toDouble())
                }

                velocity /= 1.0666f

                post(this)
            } else {
                isFlinging = false
            }
        }
    }

    public override fun onSaveInstanceState(): Parcelable {
        val superState = super.onSaveInstanceState()
        val ss = SavedState(superState)
        ss.lastAngle = lastAngle
        ss.isMenuShowing = isMenuShowing
        return ss
    }

    public override fun onRestoreInstanceState(state: Parcelable) {
        val ss = state as SavedState
        super.onRestoreInstanceState(ss.superState)
        lastAngle = ss.lastAngle
        isMenuShowing = ss.isMenuShowing
    }

    private class SavedState : BaseSavedState {

        var lastAngle : Double = 0.toDouble()
        var isMenuShowing: Boolean = false

        constructor(state: Parcelable) : super(state)

        private constructor(parcel: Parcel) : super(parcel) {
            lastAngle = parcel.readDouble()
            isMenuShowing = parcel.readInt() == 1
        }

        override fun writeToParcel(out: Parcel, flags: Int) {
            super.writeToParcel(out, flags)
            out.writeDouble(lastAngle)
            out.writeInt(isMenuShowing.toInt())
        }

        companion object {
            val CREATOR: Parcelable.Creator<SavedState> = object : Parcelable.Creator<SavedState> {
                override fun createFromParcel(`in`: Parcel): SavedState {
                    return SavedState(`in`)
                }

                override fun newArray(size: Int): Array<SavedState?> {
                    return arrayOfNulls(size)
                }
            }
        }
    }

    companion object {
        private val OVERSHOOT_INTERPOLATOR = OvershootInterpolator()
        private val ANTICIPATE_INTERPOLATOR = AnticipateInterpolator()
        private val INITIAL_DELAY = 0
        private val DURATION = 400
        private val DELAY_INCREMENT = 15
        private val HIDE_DIFF = 50
    }
}