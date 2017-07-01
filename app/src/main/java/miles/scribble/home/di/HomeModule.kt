package miles.scribble.home.di

import android.app.Application
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import dagger.Module
import dagger.Provides
import io.realm.Realm
import miles.scribble.dagger.activity.ActivityModule
import miles.scribble.dagger.activity.ActivityScope
import miles.scribble.home.HomeActivity
import miles.scribble.home.drawing.DrawingCurve
import miles.scribble.home.events.CircleMenuEvents
import miles.scribble.home.events.CircleMenuEventsReducer
import miles.scribble.home.viewmodel.HomeState
import miles.scribble.home.viewmodel.HomeViewModel
import miles.scribble.home.viewmodel.HomeViewModelFactory
import miles.scribble.redux.core.*

/**
 * Created by mbpeele on 6/28/17.
 */
@Module(includes = arrayOf(CircleMenuModule::class))
class HomeModule(activity: HomeActivity) : ActivityModule<HomeActivity>(activity) {

    val realm = Realm.getDefaultInstance()

    @Provides
    @ActivityScope
    fun provideRealm() = realm

    @Provides
    @ActivityScope
    fun viewModel(factory: ViewModelProvider.Factory) = ViewModelProviders.of(activity, factory)[HomeViewModel::class.java]

    @Provides
    @ActivityScope
    fun drawingCurve() = DrawingCurve(activity)

    @Provides
    @ActivityScope
    fun store() : Store<HomeState> = SimpleStore(HomeState())

    @Provides
    @ActivityScope
    fun factory(drawingCurve: DrawingCurve, application: Application, store: Store<HomeState>) : ViewModelProvider.Factory {
        return HomeViewModelFactory(drawingCurve, store, application)
    }
}

@Module
class CircleMenuModule {

    @Provides
    @ActivityScope
    fun reducer() : Reducer<CircleMenuEvents, HomeState> {
        return CircleMenuEventsReducer()
    }

    @Provides
    @ActivityScope
    fun dispatcher( store: Store<HomeState>, reducer: Reducer<CircleMenuEvents, HomeState>)
        : Dispatcher<CircleMenuEvents, HomeState> {
        return Dispatchers.create(store, reducer)
    }
}