package miles.scribble.redux.core

/**
 * Created by mbpeele on 6/30/17.
 */
interface Reducer<E : Event, S : State> {

    fun reduce(event: E, state: S) : S

}