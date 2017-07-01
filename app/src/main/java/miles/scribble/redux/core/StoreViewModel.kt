package miles.scribble.redux.core

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.ViewModel
import miles.scribble.redux.core.State
import miles.scribble.redux.core.Store

/**
 * Created by mbpeele on 6/30/17.
 */
open class StoreViewModel<S : State>(val store: Store<S>) : ViewModel() {

    val state : State
        get() = store.state

}