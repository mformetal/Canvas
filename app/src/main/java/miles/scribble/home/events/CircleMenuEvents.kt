package miles.scribble.home.events

import miles.scribble.home.viewmodel.HomeState
import miles.scribble.redux.core.Event
import miles.scribble.redux.core.Reducer

/**
 * Created by mbpeele on 6/30/17.
 */
sealed class CircleMenuEvents : Event {

    class ToggleClicked(val isMenuShowing: Boolean) : CircleMenuEvents()
    class RedoClicked : CircleMenuEvents()
    class UndoClicked : CircleMenuEvents()

}

class CircleMenuEventsReducer : Reducer<CircleMenuEvents, HomeState> {
    override fun reduce(event: CircleMenuEvents, state: HomeState): HomeState {
        return when (event) {
            is CircleMenuEvents.ToggleClicked -> {
                state.copy(isMenuOpen = event.isMenuShowing)
            }
            is CircleMenuEvents.RedoClicked -> {
                state
            }
            is CircleMenuEvents.UndoClicked -> {
                state
            }
        }
    }
}