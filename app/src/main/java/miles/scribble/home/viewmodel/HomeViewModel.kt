package miles.scribble.home.viewmodel

import android.app.Application
import android.arch.lifecycle.ViewModel
import miles.scribble.home.drawing.DrawingCurve
import miles.scribble.redux.android.StoreViewModel
import miles.scribble.redux.core.Store
import javax.inject.Inject

/**
 * Created by mbpeele on 6/28/17.
 */
class HomeViewModel(val drawingCurve: DrawingCurve,
                    application: Application,
                    store: Store<HomeState>) : StoreViewModel<HomeState>(application, store) {
}