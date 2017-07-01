package miles.scribble.home.di

import android.app.Application
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import dagger.Module
import dagger.Provides
import dagger.Subcomponent
import io.realm.Realm
import miles.scribble.dagger.activity.ActivityComponent
import miles.scribble.dagger.activity.ActivityComponentBuilder
import miles.scribble.dagger.activity.ActivityModule
import miles.scribble.dagger.activity.ActivityScope
import miles.scribble.home.HomeActivity
import miles.scribble.home.drawing.DrawingCurve
import miles.scribble.home.viewmodel.HomeState
import miles.scribble.home.viewmodel.HomeViewModel
import miles.scribble.home.viewmodel.HomeViewModelFactory
import miles.scribble.redux.core.SimpleStore
import miles.scribble.redux.core.Store
import miles.scribble.ui.widget.CanvasLayout
import miles.scribble.ui.widget.CanvasSurface

/**
 * Created by mbpeele on 6/30/17.
 */
/**
 * Created by mbpeele on 6/28/17.
 */
@Module
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

/**
 * Created by mbpeele on 6/28/17.
 */
@ActivityScope
@Subcomponent(modules = arrayOf(HomeModule::class))
interface HomeComponent : ActivityComponent<HomeActivity> {

    fun viewModel() : HomeViewModel

    fun canvasLayoutComponent(canvasLayoutModule: CanvasLayoutModule) : CanvasLayoutComponent

    fun canvasSurfaceComponent(canvasSurfaceModule: CanvasSurfaceModule) : CanvasSurfaceComponent

    fun circleMenuComponent(circleMenuModule: CircleMenuModule) : CircleMenuComponent

    @Subcomponent.Builder
    interface Builder : ActivityComponentBuilder<HomeModule, HomeComponent>

}