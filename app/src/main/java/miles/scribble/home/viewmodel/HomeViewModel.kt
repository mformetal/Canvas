package miles.scribble.home.viewmodel

import miles.scribble.home.drawing.DrawingCurve
import miles.scribble.redux.core.StoreViewModel
import miles.scribble.redux.core.Store
import javax.inject.Inject

/**
 * Created by mbpeele on 6/28/17.
 */
class HomeViewModel @Inject constructor(homeStore: HomeStore)
    : StoreViewModel<HomeState, HomeStore>(homeStore) {

    fun resize(width: Int, height: Int) {
        store.state.drawingCurve.resize(width, height)
    }
}