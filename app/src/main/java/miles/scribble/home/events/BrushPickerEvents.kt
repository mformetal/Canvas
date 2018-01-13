package miles.scribble.home.events

import android.graphics.Paint
import miles.dispatch.core.Event
import miles.dispatch.core.Reducer
import miles.scribble.home.viewmodel.HomeState

/**
 * Created from mbpeele on 7/29/17.
 */
sealed class BrushPickerEvents : Event {
    class BrushChosen(val paint: Paint) : BrushPickerEvents()
}

class BrushPickerReducer : Reducer<BrushPickerEvents, HomeState> {
    override fun reduce(event: BrushPickerEvents, state: HomeState): HomeState {
        return when (event) {
            is BrushPickerEvents.BrushChosen -> {
                state.copy(paint = event.paint, isMenuOpen = false)
            }
        }
    }
}