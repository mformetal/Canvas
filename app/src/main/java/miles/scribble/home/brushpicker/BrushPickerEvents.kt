package miles.scribble.home.brushpicker

import android.graphics.Paint
import miles.scribble.home.viewmodel.HomeState
import miles.scribble.redux.core.Event
import miles.scribble.redux.core.Reducer

/**
 * Created by mbpeele on 7/29/17.
 */
sealed class BrushPickerEvents : Event {
    class BrushChosen(val paint: Paint) : BrushPickerEvents()
}

class BrushPickerReducer : Reducer<BrushPickerEvents, HomeState> {
    override fun reduce(event: BrushPickerEvents, state: HomeState): HomeState {
        return when (event) {
            is BrushPickerEvents.BrushChosen -> {
                state.copy(paint = event.paint)
            }
        }
    }
}