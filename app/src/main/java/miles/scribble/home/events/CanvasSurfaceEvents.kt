package miles.scribble.home.events

import android.view.MotionEvent
import miles.dispatch.core.Event
import miles.dispatch.core.Reducer
import miles.scribble.home.drawing.CanvasMotionEventHandler
import miles.scribble.home.viewmodel.HomeState

/**
 * Created using mbpeele on 6/30/17.
 */
sealed class CanvasSurfaceEvents : Event {

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