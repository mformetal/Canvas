package miles.scribble.redux.core

import miles.scribble.redux.core.Reducer
import miles.scribble.redux.core.Store

@Suppress("AddVarianceModifier")
interface Dispatcher<E : Event, A : Any> {

    fun dispatch(event: E) : A

}

class SimpleDispatcher<E : Event, S: State>(val store: Store<S>,
                                               val reducer: Reducer<E, S>) : Dispatcher<E, E> {

    override fun dispatch(event: E) : E {
        store.state = reducer.reduce(event, store.state)
        return event
    }

}

object Dispatchers {

    fun <E: Event, S: State> create(store: Store<S>, reducer: Reducer<E, S>) : Dispatcher<E, E> {
        return SimpleDispatcher(store, reducer)
    }
}