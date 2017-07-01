package miles.scribble.ui.widget.circlemenu

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.annotation.SuppressLint
import android.content.Context
import android.support.design.widget.FloatingActionButton
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnticipateInterpolator
import android.view.animation.DecelerateInterpolator
import android.view.animation.OvershootInterpolator

import java.util.ArrayList
import java.util.Collections
import java.util.Comparator

import butterknife.BindView
import butterknife.BindViews
import butterknife.ButterKnife
import butterknife.OnClick
import miles.scribble.R
import miles.scribble.home.HomeActivity
import miles.scribble.home.di.CircleMenuModule
import miles.scribble.home.events.CircleMenuEvents
import miles.scribble.home.viewmodel.HomeState
import miles.scribble.home.viewmodel.HomeViewModel
import miles.scribble.redux.core.Dispatcher
import miles.scribble.util.Circle
import miles.scribble.util.ViewUtils
import miles.scribble.util.extensions.radius
import javax.inject.Inject

/**
 * Created by milespeele on 8/7/15.
 */
class CircleMenu : ViewGroup {

    @Inject
    lateinit var viewModel : HomeViewModel
    @Inject
    lateinit var dispatcher : Dispatcher<CircleMenuEvents, HomeState>

    @BindView(R.id.menu_toggle)
    internal lateinit var toggle: FloatingActionButton
    @BindView(R.id.menu_erase)
    internal lateinit var eraser: FloatingActionButton
    @BindView(R.id.menu_upload)
    internal lateinit var saver: FloatingActionButton

    @BindViews(R.id.menu_upload, R.id.menu_text, R.id.menu_stroke_color, R.id.menu_canvas_color,
            R.id.menu_ink, R.id.menu_brush, R.id.menu_undo, R.id.menu_redo, R.id.menu_erase, R.id.menu_image)
    internal lateinit var buttonsList: List<@JvmSuppressWildcards FloatingActionButton>

    private lateinit var circle: Circle
    private var clickedItem: FloatingActionButton? = null
    private val itemPositions: ArrayList<ItemPosition> = ArrayList()
    private val gestureDetector: GestureDetector = GestureDetector(context, GestureListener())

    private var isMenuShowing = false
    private var isAnimating = false
    private var isDragging = false
    private var isFlinging = false
    private var lastAngle: Double = 0.toDouble()
    private var startY: Float = 0.toFloat()

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
        (context as HomeActivity).component.circleMenuComponent(CircleMenuModule()).injectMembers(this)

        setWillNotDraw(false)
        descendantFocusability = ViewGroup.FOCUS_AFTER_DESCENDANTS
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        ButterKnife.bind(this)
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
        if (!itemPositions.isEmpty()) {
            return
        }

        val lps = toggle.layoutParams as MarginLayoutParams

        toggle.layout(r / 2 - toggle.measuredWidth / 2,
                measuredHeight - toggle.measuredHeight - lps.bottomMargin,
                r / 2 + toggle.measuredWidth / 2,
                measuredHeight - lps.bottomMargin)

        circle = Circle(ViewUtils.relativeCenterX(toggle), ViewUtils.relativeCenterY(toggle),
                toggle.measuredHeight * 3.75f)
        itemPositions.add(ItemPosition(toggle, cx.toDouble(), cy.toDouble(), toggle.radius()))

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

                val position = ItemPosition(child, x, y, child.radius())
                itemPositions.add(position)

                child.x -= position.mItemCircle.cx - cx
                child.y -=  position.mItemCircle.cy - cy
                child.alpha = 0f
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

                startY = y
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

    private fun rotateToggleOpen() {
        ObjectAnimator.ofFloat(toggle, View.ROTATION,
                toggle.rotation, toggle.rotation - 135f).start()
    }

    private fun rotateToggleClosed() {
        ObjectAnimator.ofFloat(toggle, View.ROTATION,
                toggle.rotation, toggle.rotation - 135f)
                .setDuration((HIDE_DIFF + DURATION + DELAY_INCREMENT * buttonsList.size).toLong())
                .start()
    }

    private fun updateItemPositions(rotater: Double) {
        for (itemPosition in itemPositions) {
            itemPosition.update(rotater)
        }
    }

    private fun getClickedItem(x: Float, y: Float): FloatingActionButton? {
        return itemPositions.firstOrNull { it.contains(x, y) }?.mView
    }

    @OnClick(R.id.menu_toggle)
    fun toggleMenu() {
        if (!isAnimating) {
            dispatcher.dispatch(CircleMenuEvents.ToggleClicked(isMenuShowing))

            if (isMenuShowing) {
                hide()
            } else {
                show()
            }
        }
    }

    private fun dispatchClickEvent(view: View) {
        when (view.id) {
            R.id.menu_redo -> {
                dispatcher.dispatch(CircleMenuEvents.RedoClicked())
            }
            R.id.menu_undo -> {
                dispatcher.dispatch(CircleMenuEvents.UndoClicked())
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
                val view = position.mView

                if (view.id == R.id.menu_toggle) {
                    continue
                }

                val diffX = position.mItemCircle.cx - cx
                val diffY = position.mItemCircle.cy - cy

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
                val view = position.mView

                if (view.id == R.id.menu_toggle) {
                    continue
                }

                val diffX = position.mItemCircle.cx - cx
                val diffY = position.mItemCircle.cy - cy

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

    private inner class ItemPosition(val mView: FloatingActionButton, itemX: Double, itemY: Double, radius: Float) {

        val mItemCircle: Circle = Circle(itemX.toFloat(), itemY.toFloat(), radius)

        fun update(matrixAngle: Double) {
            val angleInRads = Math.toRadians(matrixAngle)

            val cosAngle = Math.cos(angleInRads)
            val sinAngle = Math.sin(angleInRads)

            val dx = mItemCircle.cx - cx
            val dy = mItemCircle.cy - cy

            var rx = (dx * cosAngle - dy * sinAngle).toFloat()
            var ry = (dx * sinAngle + dy * cosAngle).toFloat()

            rx += cx
            ry += cy

            mItemCircle.setCenterX(rx)
            mItemCircle.setCenterY(ry)

            val radius = mItemCircle.radius

            mView.x = rx - radius
            mView.y = ry - radius
        }

        fun contains(x: Float, y: Float): Boolean {
            return mItemCircle.contains(x, y)
        }
    }

    private inner class ItemPositionComparator : Comparator<ItemPosition> {

        override fun compare(lhs: ItemPosition, rhs: ItemPosition): Int {
            val left = lhs.mItemCircle
            val right = rhs.mItemCircle
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

    companion object {
        private val OVERSHOOT_INTERPOLATOR = OvershootInterpolator()
        private val ANTICIPATE_INTERPOLATOR = AnticipateInterpolator()
        private val INITIAL_DELAY = 0
        private val DURATION = 400
        private val DELAY_INCREMENT = 15
        private val HIDE_DIFF = 50
    }
}