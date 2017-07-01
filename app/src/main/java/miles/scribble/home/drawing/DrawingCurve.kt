package miles.scribble.home.drawing

import android.graphics.Canvas
import android.view.MotionEvent
import javax.inject.Inject
import javax.inject.Singleton

import miles.scribble.home.viewmodel.HomeState
import miles.scribble.home.viewmodel.HomeViewModel

/**
 * Created by mbpeele on 9/25/15.
 */
class DrawingCurve(private val viewModel: HomeViewModel) {

    private val state: HomeState
        get() = viewModel.state

    fun drawToSurfaceView(canvas: Canvas?) {
        canvas?.takeIf { state.isSafeToDraw }?.drawBitmap(state.bitmap, 0f, 0f, null)
    }

}
