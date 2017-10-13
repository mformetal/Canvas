package miles.scribble.util.android

import android.arch.lifecycle.ViewModel
import miles.redux.core.State
import miles.redux.core.Store

/**
 * Created using mbpeele on 6/30/17.
 */
open class StoreViewModel<S : State, out SR : Store<S>>(val store: SR) : ViewModel() {

    val state : S
        get() = store.state

}