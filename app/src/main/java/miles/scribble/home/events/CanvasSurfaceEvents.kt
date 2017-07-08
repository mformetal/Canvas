package miles.scribble.home.events

import android.view.MotionEvent
import miles.scribble.home.viewmodel.HomeState
import miles.scribble.redux.core.Event
import miles.scribble.redux.core.Reducer
import android.graphics.Bitmap
import android.graphics.Canvas
import miles.scribble.home.drawing.Stroke
import miles.scribble.home.drawing.redrawable.RedrawableLines
import miles.scribble.util.extensions.copy


/**
 * Created by mbpeele on 6/30/17.
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

class CanvasSurfaceReducer : Reducer<CanvasSurfaceEvents, HomeState> {
    override fun reduce(event: CanvasSurfaceEvents, state: HomeState): HomeState {
        return when (event) {
            is CanvasSurfaceEvents.Resize -> {
                val (width, height) = event
                val scaledBitmap = Bitmap.createScaledBitmap(state.bitmap, width, height, false)
                state.copy(width = width, height = height, bitmap = scaledBitmap, canvas = Canvas(scaledBitmap))
            }
            is CanvasSurfaceEvents.TouchDown -> {
                val x = event.motionEvent.x
                val y = event.motionEvent.y

                when (state.drawType) {
                    HomeState.DrawType.DRAW, HomeState.DrawType.ERASE -> {
                        state.stroke.addPoint(x, y, state.canvas, state.paint)
                        state.copy(stroke = state.stroke.copy(),
                                activePointer = event.motionEvent.getPointerId(0),
                                lastX = x, lastY = y)
                    }
                    HomeState.DrawType.TEXT -> TODO()
                    HomeState.DrawType.INK -> TODO()
                    HomeState.DrawType.PICTURE -> TODO()
                }
            }
            is CanvasSurfaceEvents.PointerDown -> {
                when (state.drawType) {
                    HomeState.DrawType.TEXT, HomeState.DrawType.PICTURE -> {
                        TODO()
                    }
                    else -> {
                        state
                    }
                }
            }
            is CanvasSurfaceEvents.TouchMove -> {
                val pointerIndex = event.motionEvent.findPointerIndex(state.activePointer)
                val x = event.motionEvent.getX(pointerIndex)
                val y = event.motionEvent.getY(pointerIndex)

                when (state.drawType) {
                    HomeState.DrawType.DRAW, HomeState.DrawType.ERASE -> {
                        val stroke = state.stroke.apply {
                            for (i in 0 until event.motionEvent.historySize) {
                                addPoint(
                                        event.motionEvent.getHistoricalX(pointerIndex, i),
                                        event.motionEvent.getHistoricalY(pointerIndex, i),
                                        state.canvas, state.paint)
                            }
                            addPoint(x, y, state.canvas, state.paint)
                        }

                        state.copy(stroke = stroke.copy(), lastX = x, lastY = y)
                    }
                    HomeState.DrawType.TEXT -> TODO()
                    HomeState.DrawType.INK -> TODO()
                    HomeState.DrawType.PICTURE -> TODO()
                }
            }
            is CanvasSurfaceEvents.TouchUp -> {
                when (state.drawType) {
                    HomeState.DrawType.DRAW, HomeState.DrawType.ERASE -> {
                        val history = state.history.copy().apply {
                            push(RedrawableLines(state.stroke.points, state.paint))
                        }
                        state.copy(history = history, stroke = Stroke(),
                                lastX = event.motionEvent.x, lastY = event.motionEvent.y)
                    }
                    HomeState.DrawType.TEXT -> TODO()
                    HomeState.DrawType.INK -> TODO()
                    HomeState.DrawType.PICTURE -> TODO()
                }
            }
            is CanvasSurfaceEvents.PointerUp -> {
                val pointerIndex = event.motionEvent.action and MotionEvent.ACTION_POINTER_INDEX_MASK shr MotionEvent.ACTION_POINTER_INDEX_SHIFT
                val pointerId = event.motionEvent.getPointerId(pointerIndex)

                if (pointerId == state.activePointer) {
                    val newPointerIndex = if (pointerIndex == 0) 1 else 0
                    val lastX = event.motionEvent.getX(newPointerIndex)
                    val lastY = event.motionEvent.getY(newPointerIndex)
                    val activePointer = event.motionEvent.getPointerId(newPointerIndex)
                    state.copy(lastY = lastY, lastX = lastX, activePointer = activePointer)
                } else {
                    state
                }
            }
            is CanvasSurfaceEvents.MotionCancel -> {
                state.copy(activePointer = HomeState.INVALID_POINTER)
            }
        }
    }
}