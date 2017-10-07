package miles.redux.core

/**
 * Created by mbpeele on 6/30/17.
 */
@Suppress("AddVarianceModifier")
interface Reducer<E : Event, S : State> {

    fun reduce(event: E, state: S) : S

}