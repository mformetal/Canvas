package miles.scribble.home.events

import miles.scribble.home.viewmodel.HomeState
import miles.scribble.redux.core.Event
import miles.scribble.redux.core.Reducer

/**
 * Created by mbpeele on 6/30/17.
 */
sealed class CanvasSurfaceEvents : Event {



}

class CanvasSurfaceReducer : Reducer<CanvasSurfaceEvents, HomeState> {
    override fun reduce(event: CanvasSurfaceEvents, state: HomeState): HomeState {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}