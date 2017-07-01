package miles.scribble.redux.core

import java.util.concurrent.CopyOnWriteArrayList
import kotlin.properties.Delegates

interface State

interface StateChangeListener<in S : State> {

    fun onStateChanged(state: S)

}

interface Store<S : State> {

    var state: S

    fun subscribe(stateChangeListener: StateChangeListener<S>)

    fun unsubscribe(stateChangeListener: StateChangeListener<S>)
}

open class SimpleStore<S : State>(initialState: S,
                             private val subscribers: CopyOnWriteArrayList<StateChangeListener<S>> =CopyOnWriteArrayList()) : Store<S> {

    override var state by Delegates.observable(initialState, {
        _, _, _ ->
        notifySubscribers()
    })

    init {
        notifySubscribers()
    }

    private fun notifySubscribers() {
        subscribers.forEach { it.onStateChanged(state) }
    }

    override fun subscribe(stateChangeListener: StateChangeListener<S>) {
        subscribers.add(stateChangeListener)
    }

    override fun unsubscribe(stateChangeListener: StateChangeListener<S>) {
        subscribers.remove(stateChangeListener)
    }
}