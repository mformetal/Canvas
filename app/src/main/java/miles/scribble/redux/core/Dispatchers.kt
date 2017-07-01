package miles.scribble.redux.core

import miles.scribble.redux.core.Reducer
import miles.scribble.redux.core.Store

/**
 * Created by mbpeele on 6/30/17.
 */
interface Dispatcher<E : Event, S: State> {

    fun dispatch(event: E)

}

class SimpleDispatcher<E : Event, S: State>(val store: Store<S>,
                                               val reducer: Reducer<E, S>) : Dispatcher<E, S> {

    override fun dispatch(event: E) {
        store.state = reducer.reduce(event, store.state)
    }

}

object Dispatchers {

    fun <E: Event, S: State> create(store: Store<S>, reducer: Reducer<E, S>) : Dispatcher<E, S> {
        return SimpleDispatcher(store, reducer)
    }
}