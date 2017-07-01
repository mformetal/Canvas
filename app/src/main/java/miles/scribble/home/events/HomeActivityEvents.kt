package miles.scribble.home.events

import miles.scribble.home.viewmodel.HomeState
import miles.scribble.redux.core.Event
import miles.scribble.redux.core.Reducer

/**
 * Created by mbpeele on 7/1/17.
 */
sealed class HomeActivityEvents : Event {

    class Resize(val width: Int, val height: Int) : HomeActivityEvents()

}

class HomeActivityReducer : Reducer<HomeActivityEvents, HomeState> {
    override fun reduce(event: HomeActivityEvents, state: HomeState): HomeState {
        return when (event) {
            is HomeActivityEvents.Resize -> {
//                state.copy(width = event.width, height = event.height)
                state
            }
        }
    }

}