package miles.scribble.ui.widget

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View

import javax.inject.Inject

import miles.scribble.home.HomeActivity
import miles.scribble.home.di.CanvasSurfaceModule
import miles.scribble.home.events.CanvasSurfaceEvents
import miles.scribble.home.viewmodel.HomeState
import miles.scribble.home.viewmodel.HomeViewModel
import miles.scribble.redux.core.Dispatcher

/**
 * Created by Miles Peele on 10/2/2015.
 */
class CanvasSurface : SurfaceView, SurfaceHolder.Callback {

    @Inject
    lateinit var viewModel: HomeViewModel
    @Inject
    lateinit var dispatcher : Dispatcher<CanvasSurfaceEvents, HomeState>
    private lateinit var drawingThread: DrawingThread

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init()
    }

    fun init() {
        (context as HomeActivity).component.canvasSurfaceComponent(CanvasSurfaceModule()).injectMembers(this)

        setLayerType(View.LAYER_TYPE_NONE, null)

        setWillNotDraw(false)
        isSaveEnabled = true

        holder.addCallback(this)
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        holder.setFixedSize(width, height)

        drawingThread = DrawingThread(holder)
        drawingThread.setRunning(true)
        drawingThread.start()
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {}

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        drawingThread.onDestroy()
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        return !isEnabled && viewModel.onTouchEvent(event)
    }

    private inner class DrawingThread(private val mSurfaceHolder: SurfaceHolder) : Thread("drawingThread") {

        private var mRun = false
        private val mRunLock = Any()

        fun setRunning(b: Boolean) {
            synchronized(mRunLock) {
                mRun = b
            }
        }

        override fun run() {
            while (mRun) {
                var c: Canvas? = null
                try {
                    if (mSurfaceHolder.surface.isValid) {
                        c = mSurfaceHolder.lockCanvas()
                    }

                    synchronized(mSurfaceHolder) {
                        synchronized(mRunLock) {
                            c?.let {
                                if (mRun) {
                                    viewModel.drawToSurfaceView(it)
                                }
                            }
                        }
                    }
                } catch (e: IllegalArgumentException) {

                } finally {
                    if (c != null) {
                        mSurfaceHolder.unlockCanvasAndPost(c)
                    }
                }
            }
        }

        fun onDestroy() {
            var retry = true
            setRunning(false)
            while (retry) {
                try {
                    join()
                    retry = false
                } catch (e: InterruptedException) {

                }

            }
        }
    }
}