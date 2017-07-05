package miles.scribble.redux.core

import io.reactivex.Flowable
import io.reactivex.FlowableSubscriber
import io.reactivex.Observable
import miles.scribble.home.events.CircleMenuEvents
import miles.scribble.home.viewmodel.HomeState
import org.reactivestreams.Subscription

/**
 * Created by mbpeele on 6/30/17.
 */
@Suppress("AddVarianceModifier")
interface Reducer<E : Event, S : State> {

    fun reduce(event: E, state: S) : S

}