package miles.scribble.home.events

import android.graphics.Paint
import miles.scribble.home.viewmodel.HomeState
import miles.scribble.redux.core.Event
import miles.scribble.redux.core.Reducer

/**
 * Created by mbpeele on 7/22/17.
 */
sealed class ColorPickerEvents : Event {

    class StrokeColorChosen(val color: Int) : ColorPickerEvents()
    class BackgroundColorChosen(val color: Int) : ColorPickerEvents()

}

class ColorPickerReducer : Reducer<ColorPickerEvents, HomeState> {
    override fun reduce(event: ColorPickerEvents, state: HomeState): HomeState {
        return when (event) {
            is ColorPickerEvents.StrokeColorChosen -> {
                val paint = Paint(state.paint).apply {
                    color = event.color
                }
                state.copy(paint = paint, strokeColor = event.color, isMenuOpen = false)
            }
            is ColorPickerEvents.BackgroundColorChosen -> {
                val backgroundColor = event.color
                state.bitmap.eraseColor(backgroundColor)
                state.copy(backgroundColor = backgroundColor, isMenuOpen = false)
            }
        }
    }
}