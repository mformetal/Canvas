package miles.scribble.ui.widget

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.BlurMaskFilter
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.os.Handler
import android.support.design.widget.CoordinatorLayout
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.Toolbar

import butterknife.BindView
import butterknife.ButterKnife
import io.reactivex.disposables.Disposable
import miles.scribble.R
import miles.scribble.home.HomeActivity
import miles.scribble.home.di.CanvasLayoutModule
import miles.scribble.home.drawing.CanvasPoint
import miles.scribble.home.viewmodel.HomeState
import miles.scribble.home.viewmodel.HomeViewModel
import miles.scribble.redux.core.StateChangeListener
import miles.scribble.redux.rx.flowable
import miles.scribble.util.ViewUtils
import javax.inject.Inject

/**
 * Created by milespeele on 8/7/15.
 */
class CanvasLayout : CoordinatorLayout {

    lateinit var flowableListener : Disposable

    @Inject
    lateinit var homeViewModel : HomeViewModel

    @BindView(R.id.canvas_surface)
    internal lateinit var surface: CanvasSurface
    @BindView(R.id.canvas_fab_menu)
    internal lateinit var circleMenu: CircleMenu
    @BindView(R.id.canvas_toolbar)
    internal lateinit var toolbar: Toolbar

    private val mRect = Rect()
    private lateinit var mShadowPaint: Paint
    private val mHandler: Handler = Handler()
    private var radius: Float = 0.toFloat()
        set(radius) {
            field = radius
            invalidate()
        }

    private var paintAlpha: Int
        get() = mShadowPaint.alpha
        set(alpha) {
            mShadowPaint.alpha = alpha
            invalidate()
        }

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
        (context as HomeActivity).component.canvasLayoutComponent(CanvasLayoutModule()).injectMembers(this)

        mShadowPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        mShadowPaint.color = Color.BLACK
        mShadowPaint.alpha = 0
        mShadowPaint.maskFilter = BlurMaskFilter(15f, BlurMaskFilter.Blur.OUTER)

        setWillNotDraw(false)
        clipChildren = false
        isSaveEnabled = true
    }

    override fun onFinishInflate() {
        super.onFinishInflate()

        ButterKnife.bind(this)

        flowableListener = flowable(homeViewModel.store)
                .subscribe {
                    if (it.isMenuOpen) {
                        dim()
                    } else {
                        undim()
                    }
                }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        flowableListener.dispose()
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        when (ev.action) {
            MotionEvent.ACTION_DOWN -> {
                if (isSystemUISwipe(ev)) {
                    return true
                }

                if (mShadowPaint.alpha != 0) {
                    surface.isEnabled = false
                }

                if (!circleMenu.isEnabled) {
                    circleMenu.isEnabled = true
                }
            }
            MotionEvent.ACTION_MOVE -> mHandler.postDelayed({ ViewUtils.systemUIGone(rootView) }, 350)
        }

        return false
    }

    override fun draw(canvas: Canvas) {
        super.draw(canvas)

        canvas.drawCircle(circleMenu.cx, circleMenu.cy + (height - circleMenu.height),
                radius, mShadowPaint)
    }

    private fun dim() {
        if (ALPHA.get(this) != 64) {
            val alpha = ObjectAnimator.ofInt(this, ALPHA, 64).setDuration(200)

            // Radius is distance from the middle of toggle button to the top-left corner of the view
            // Using CanvasPoint because it already has the distance formula defined and implemented
            val rcx = width / 2f
            val rcy = IntArray(2).run {
                circleMenu.toggle.getLocationOnScreen(this)
                this.last().toFloat() + circleMenu.toggle.height / 2F
            }
            val togglePoint = CanvasPoint(rcx, rcy)
            val targetPoint = CanvasPoint(0f, 0f)

            val radius = ObjectAnimator.ofFloat(this, RADIUS, togglePoint.computeDistance(targetPoint)).setDuration(200)

            val visibility = ViewUtils.visibleAnimator(toolbar)

            val set = AnimatorSet()
            set.playTogether(alpha, radius, visibility)
            set.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationStart(animation: Animator) {
                    surface.isEnabled = false
                }
            })
            set.start()
        }
    }

    private fun undim() {
        if (ALPHA.get(this) != 0) {
            val alpha = ObjectAnimator.ofInt(this, ALPHA, 0).setDuration(400)

            val radius = ObjectAnimator.ofFloat(this, RADIUS, 0F).setDuration(400)

            val visibility = ViewUtils.goneAnimator(toolbar)

            val set = AnimatorSet()
            set.playTogether(alpha, radius, visibility)
            set.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    surface.isEnabled = true
                }
            })
            set.start()
        }
    }

    private fun isSystemUISwipe(event: MotionEvent): Boolean {
        val scrim = resources.getDimension(R.dimen.system_ui_scrim)

        if (event.y <= scrim) {
            return true
        }

        return event.y >= height - scrim
    }

    companion object {

        private val ALPHA = object : ViewUtils.IntProperty<CanvasLayout>("alpha") {

            override fun setValue(receiver: CanvasLayout, value: Int) {
                receiver.paintAlpha = value
            }

            override fun get(layout: CanvasLayout): Int {
                return layout.paintAlpha
            }
        }

        private val RADIUS = object : ViewUtils.FloatProperty<CanvasLayout>("radius") {
            override fun setValue(receiver: CanvasLayout, value: Float) {
                receiver.radius = value
            }

            override fun get(`object`: CanvasLayout): Float {
                return `object`.radius
            }
        }
    }
}