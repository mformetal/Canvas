package miles.scribble.home.events

import android.view.MotionEvent
import miles.scribble.home.viewmodel.HomeState
import miles.redux.core.Event
import miles.redux.core.Reducer
import android.graphics.Bitmap
import android.graphics.Canvas
import miles.scribble.home.drawing.CanvasMotionEventHandler


/**
 * Created using mbpeele on 6/30/17.
 */
sealed class CanvasSurfaceEvents : Event {

    data class Resize(val width: Int, val height: Int) : CanvasSurfaceEvents()
    class TouchDown(val motionEvent: MotionEvent) : CanvasSurfaceEvents()
    class PointerDown(val motionEvent: MotionEvent) : CanvasSurfaceEvents()
    class TouchMove(val motionEvent: MotionEvent) : CanvasSurfaceEvents()
    class TouchUp(val motionEvent: MotionEvent) : CanvasSurfaceEvents()
    class PointerUp(val motionEvent: MotionEvent) : CanvasSurfaceEvents()
    class MotionCancel(val motionEvent: MotionEvent) : CanvasSurfaceEvents()

}

class CanvasSurfaceReducer(val canvasMotionEventHandler: CanvasMotionEventHandler) : Reducer<CanvasSurfaceEvents, HomeState> {
    override fun reduce(event: CanvasSurfaceEvents, state: HomeState): HomeState {
        return when (event) {
            is CanvasSurfaceEvents.Resize -> {
                val (width, height) = event
                val scaledBitmap = Bitmap.createScaledBitmap(state.bitmap, width, height, false)
                state.copy(bitmap = scaledBitmap, canvas = Canvas(scaledBitmap))
            }
            is CanvasSurfaceEvents.TouchDown -> {
                canvasMotionEventHandler.handleTouchDown(event.motionEvent, state)
            }
            is CanvasSurfaceEvents.PointerDown -> {
                canvasMotionEventHandler.handlePointerDown(event.motionEvent, state)
            }
            is CanvasSurfaceEvents.TouchMove -> {
                canvasMotionEventHandler.handleTouchMove(event.motionEvent, state)
            }
            is CanvasSurfaceEvents.TouchUp -> {
                canvasMotionEventHandler.handleTouchUp(event.motionEvent, state)
            }
            is CanvasSurfaceEvents.PointerUp -> {
               canvasMotionEventHandler.handlePointerUp(event.motionEvent, state)
            }
            is CanvasSurfaceEvents.MotionCancel -> {
                canvasMotionEventHandler.handleMotionCancel(state)
            }
        }
    }
}