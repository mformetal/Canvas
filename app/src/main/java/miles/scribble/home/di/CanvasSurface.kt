package miles.scribble.home.di

import dagger.MembersInjector
import dagger.Module
import dagger.Provides
import dagger.Subcomponent
import miles.scribble.dagger.ViewScope
import miles.scribble.home.events.CanvasSurfaceEvents
import miles.scribble.home.events.CanvasSurfaceReducer
import miles.scribble.home.viewmodel.HomeState
import miles.scribble.home.viewmodel.HomeViewModel
import miles.scribble.redux.core.Dispatcher
import miles.scribble.redux.core.Dispatchers
import miles.scribble.redux.core.Reducer
import miles.scribble.redux.core.Store
import miles.scribble.ui.widget.CanvasSurface

/**
 * Created by mbpeele on 6/30/17.
 */
@ViewScope
@Subcomponent(modules = arrayOf(CanvasSurfaceModule::class))
interface CanvasSurfaceComponent : MembersInjector<CanvasSurface>

@Module
class CanvasSurfaceModule {

    @Provides
    @ViewScope
    fun reducer() : Reducer<CanvasSurfaceEvents, HomeState> {
        return CanvasSurfaceReducer()
    }

    @Provides
    @ViewScope
    fun dispatcher(homeViewModel: HomeViewModel, reducer: Reducer<CanvasSurfaceEvents, HomeState>) : Dispatcher<CanvasSurfaceEvents, HomeState> {
        return Dispatchers.create(homeViewModel.store, reducer)
    }
}