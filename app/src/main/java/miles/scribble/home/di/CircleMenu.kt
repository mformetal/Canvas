package miles.scribble.home.di

import dagger.MembersInjector
import dagger.Module
import dagger.Provides
import dagger.Subcomponent
import miles.scribble.dagger.ViewScope
import miles.scribble.home.events.CircleMenuEvents
import miles.scribble.home.events.CircleMenuEventsReducer
import miles.scribble.home.viewmodel.HomeState
import miles.scribble.home.viewmodel.HomeViewModel
import miles.scribble.redux.core.Dispatcher
import miles.scribble.redux.core.Dispatchers
import miles.scribble.redux.core.Reducer
import miles.scribble.ui.widget.circlemenu.CircleMenu

/**
 * Created by mbpeele on 6/30/17.
 */
@ViewScope
@Subcomponent(modules = arrayOf(CircleMenuModule::class))
interface CircleMenuComponent : MembersInjector<CircleMenu>

@Module
class CircleMenuModule {

    @Provides
    @ViewScope
    fun reducer() : Reducer<CircleMenuEvents, HomeState> {
        return CircleMenuEventsReducer()
    }

    @Provides
    @ViewScope
    fun dispatcher(homeViewModel: HomeViewModel, reducer: Reducer<CircleMenuEvents, HomeState>)
            : Dispatcher<CircleMenuEvents, HomeState> {
        return Dispatchers.create(homeViewModel.store, reducer)
    }
}