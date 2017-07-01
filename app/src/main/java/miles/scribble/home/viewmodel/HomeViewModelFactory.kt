package miles.scribble.home.viewmodel

import android.app.Application
import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import miles.scribble.home.drawing.DrawingCurve
import miles.scribble.redux.core.Store

/**
 * Created by mbpeele on 6/30/17.
 */
class HomeViewModelFactory(val drawingCurve: DrawingCurve,
                           val store: Store<HomeState>,
                           val application: Application) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(p0: Class<T>?): T {
        return HomeViewModel(drawingCurve, application, store) as T
    }
}