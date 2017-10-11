package miles.scribble.home.events

import android.graphics.*
import miles.scribble.home.drawing.DrawType
import miles.scribble.home.viewmodel.HomeState
import miles.redux.core.Event
import miles.redux.core.Reducer

/**
 * Created using mbpeele on 6/30/17.
 */
sealed class CircleMenuEvents : Event {

    class ToggleClicked(val isMenuShowing: Boolean) : CircleMenuEvents()
    class StrokeColorClicked : CircleMenuEvents()
    class BackgroundColorClicked : CircleMenuEvents()
    class EraserClicked(val isErasing: Boolean) : CircleMenuEvents()
    class InkClicked : CircleMenuEvents()
    class BrushClicked : CircleMenuEvents()
    class PictureClicked : CircleMenuEvents()

    class RedrawStarted(val isUndo: Boolean) : CircleMenuEvents()
    class Redraw : CircleMenuEvents()
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
                workerBitmap = Bitmap.createBitmap(state.bitmap).apply {
                    eraseColor(state.backgroundColor)
                }
                workerCanvas = Canvas(workerBitmap)

                if (event.isUndo) {
                    state.history.undo()
                } else {
                    state.history.redo()
                }
                state.copy(isSafeToDraw = false)
            }
            is CircleMenuEvents.Redraw -> {
                state.history.redraw(workerCanvas)
                state
            }
            is CircleMenuEvents.RedrawCompleted -> {
                val bitmap = workerBitmap.copy(Bitmap.Config.ARGB_8888, true)
                workerBitmap.recycle()
                state.copy(isSafeToDraw = true, bitmap = bitmap, canvas = Canvas(bitmap))
            }
            is CircleMenuEvents.StrokeColorClicked -> {
                state.onClickSubject.onNext(event)
                state
            }
            is CircleMenuEvents.BackgroundColorClicked -> {
                state.onClickSubject.onNext(event)
                state
            }
            is CircleMenuEvents.EraserClicked -> {
                if (event.isErasing) {
                    state.copy(drawType = DrawType.ERASE,
                            paint = Paint(state.paint).apply {
                                color = state.backgroundColor
                                strokeWidth = 20f
                            })
                } else {
                    state.copy(drawType = DrawType.NORMAL,
                            paint = Paint(state.paint).apply {
                                color = state.strokeColor
                                strokeWidth = HomeState.STROKE_WIDTH
                            }, isMenuOpen = false)
                }
            }
            is CircleMenuEvents.InkClicked -> {
                state.copy(drawType = DrawType.INK,
                        lastX = state.bitmap.width / 2f,
                        lastY = state.bitmap.height / 2f,
                        isMenuOpen = false)
            }
            is CircleMenuEvents.BrushClicked -> {
                state.onClickSubject.onNext(event)
                state
            }
            is CircleMenuEvents.PictureClicked -> {
                state.onClickSubject.onNext(event)
                state
            }
        }
    }
}