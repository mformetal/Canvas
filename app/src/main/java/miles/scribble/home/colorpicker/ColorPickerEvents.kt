package miles.scribble.home.colorpicker

import android.graphics.Paint
import miles.scribble.home.viewmodel.HomeState
import miles.scribble.home.viewmodel.HomeStore
import miles.scribble.redux.core.Event
import miles.scribble.redux.core.Reducer

/**
 * Created by mbpeele on 7/22/17.
 */
sealed class ColorPickerEvents : Event {

    class ColorChosen(val color: Int) : ColorPickerEvents()

}

class ColorPickerReducer : Reducer<ColorPickerEvents, HomeState> {
    override fun reduce(event: ColorPickerEvents, state: HomeState): HomeState {
        return when (event) {
            is ColorPickerEvents.ColorChosen -> {
                val paint = Paint(state.paint).apply {
                    color = event.color
                }
                state.copy(paint = paint)
            }
        }
    }
}