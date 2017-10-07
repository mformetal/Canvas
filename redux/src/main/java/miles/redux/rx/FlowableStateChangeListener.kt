package miles.redux.rx

import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.FlowableEmitter
import io.reactivex.functions.Cancellable
import miles.redux.core.State
import miles.redux.core.StateChangeListener
import miles.redux.core.Store

/**
 * Created by mbpeele on 8/12/17.
 */
fun <S : State> flowable(store: Store<S>) : Flowable<S> {
    return Flowable.create({
        // Should I emit an initial state here, or just wait for new states?
        val listener = FlowableListener(store, it)
        store.subscribe(listener)
    }, BackpressureStrategy.LATEST)
}

private class FlowableListener<in S : State>(
        private val store: Store<S>,
        private val emitter: FlowableEmitter<S>) : StateChangeListener<S>, Cancellable {

    override fun onStateChanged(state: S) {
        emitter.onNext(state)
    }

    override fun cancel() {
        store.unsubscribe(this)
    }
}