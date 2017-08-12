package miles.scribble.home.events

import android.net.Uri
import miles.scribble.home.drawing.DrawType
import miles.scribble.home.viewmodel.HomeState
import miles.scribble.redux.core.Event
import miles.scribble.redux.core.Reducer

/**
 * Created by mbpeele on 8/12/17.
 */
sealed class HomeActivityEvents : Event {
    class PictureChosen(val uri: Uri) : HomeActivityEvents()
}

class HomeActivityReducer : Reducer<HomeActivityEvents, HomeState> {
    override fun reduce(event: HomeActivityEvents, state: HomeState): HomeState {
        return when (event) {
            is HomeActivityEvents.PictureChosen -> {
                state
            }
        }
    }
}