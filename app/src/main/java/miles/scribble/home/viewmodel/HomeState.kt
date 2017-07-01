package miles.scribble.home.viewmodel

import miles.scribble.home.drawing.DrawingCurve
import miles.scribble.redux.core.SimpleStore
import miles.scribble.redux.core.State
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Created by mbpeele on 6/30/17.
 */
@Singleton
class HomeStore @Inject constructor(state: HomeState) : SimpleStore<HomeState>(state)

@Singleton
class HomeState @Inject constructor(val drawingCurve: DrawingCurve) : State {

    private var isMenuOpen : Boolean = false
        private set

    fun setIsMenuOpen(isMenuOpen: Boolean) : HomeState {
        return HomeState(drawingCurve).apply { this.isMenuOpen = isMenuOpen }
    }

}