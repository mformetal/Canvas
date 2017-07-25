package miles.scribble.home.events

import android.graphics.*
import miles.scribble.home.drawing.Stroke
import miles.scribble.home.drawing.redrawable.Redrawable
import miles.scribble.home.viewmodel.HomeState
import miles.scribble.redux.core.Event
import miles.scribble.redux.core.Reducer
import miles.scribble.util.extensions.copy
import java.util.*

/**
 * Created by mbpeele on 6/30/17.
 */
sealed class CircleMenuEvents : Event {

    class ToggleClicked(val isMenuShowing: Boolean) : CircleMenuEvents()
    class StrokeColorClicked(val id: Int) : CircleMenuEvents()
    class BackgroundColorClicked(val id: Int) : CircleMenuEvents()
    class EraserClicked(val isErasing: Boolean) : CircleMenuEvents()
    class InkClicked() : CircleMenuEvents()

    class RedrawStarted(val isUndo: Boolean) : CircleMenuEvents()
    class Redraw(val toRedraw: Redrawable) : CircleMenuEvents()
    class RedrawCompleted : CircleMenuEvents()

}

class CircleMenuEventsReducer : Reducer<CircleMenuEvents, HomeState> {

    lateinit var workerBitmap: Bitmap
    lateinit var workerCanvas: Canvas

    override fun reduce(event: CircleMenuEvents, state: HomeState): HomeState {
        return when (event) {
            is CircleMenuEvents.ToggleClicked -> {
                state.copy(isMenuOpen = event.isMenuShowing)
            }
            is CircleMenuEvents.RedrawStarted -> {
                workerBitmap = Bitmap.createBitmap(state.bitmap)
                workerCanvas = Canvas(workerBitmap).apply {
                    drawColor(Color.WHITE, PorterDuff.Mode.CLEAR)
                    drawBitmap(state.cachedBitmap, 0f, 0f, null)
                }
                val history : Stack<Redrawable>
                val redoHistory : Stack<Redrawable>

                if (event.isUndo) {
                    history = state.history.copy()
                    val popped = history.pop()
                    redoHistory = state.redoHistory.copy().apply {
                        push(popped)
                    }
                } else {
                    redoHistory = state.redoHistory.copy()
                    val popped = redoHistory.pop()
                    history = state.history.copy().apply {
                        push(popped)
                    }
                }
                state.copy(isSafeToDraw = false, redoHistory = redoHistory, history = history)
            }
            is CircleMenuEvents.Redraw -> {
                event.toRedraw.draw(workerCanvas)
                state
            }
            is CircleMenuEvents.RedrawCompleted -> {
                val bitmap = workerBitmap.copy(Bitmap.Config.ARGB_8888, true)
                state.copy(isSafeToDraw = true, bitmap = bitmap, canvas = Canvas(bitmap))
            }
            is CircleMenuEvents.StrokeColorClicked -> {
                state.onClickSubject.onNext(event.id)
                state
            }
            is CircleMenuEvents.BackgroundColorClicked -> {
                state.onClickSubject.onNext(event.id)
                state
            }
            is CircleMenuEvents.EraserClicked -> {
                if (event.isErasing) {
                    state.copy(drawType = HomeState.DrawType.ERASE,
                            paint = Paint(state.paint).apply {
                                color = state.backgroundColor
                                strokeWidth = 20f
                            })
                } else {
                    state.copy(drawType = HomeState.DrawType.DRAW,
                            paint = Paint(state.paint).apply {
                                color = state.strokeColor
                                strokeWidth = HomeState.STROKE_WIDTH
                            }, isMenuOpen = false)
                }
            }
            is CircleMenuEvents.InkClicked -> {
                state.copy(drawType = HomeState.DrawType.INK,
                        lastX = state.bitmap.width / 2f,
                        lastY = state.bitmap.height / 2f,
                        isMenuOpen = false)
            }
        }
    }
}