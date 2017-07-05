package miles.scribble.redux.core

import android.os.Handler
import android.os.Looper
import android.support.annotation.UiThread
import io.reactivex.android.schedulers.AndroidSchedulers
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.properties.Delegates

interface State

interface StateChangeListener<in S : State> {

    @UiThread
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
        _, _, _ -> notifySubscribers()
    })
    private val handler : Handler = Handler(Looper.getMainLooper())

    init {
        notifySubscribers()
    }

    private fun notifySubscribers() {
        AndroidSchedulers.mainThread().scheduleDirect {
            subscribers.forEach { it.onStateChanged(state) }
        }
    }

    override fun subscribe(stateChangeListener: StateChangeListener<S>) {
        subscribers.add(stateChangeListener)
    }

    override fun unsubscribe(stateChangeListener: StateChangeListener<S>) {
        subscribers.remove(stateChangeListener)
    }
}