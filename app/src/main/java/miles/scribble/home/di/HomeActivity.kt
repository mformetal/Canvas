package miles.scribble.home.di

import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import dagger.Module
import dagger.Provides
import dagger.Subcomponent
import miles.scribble.dagger.activity.ActivityComponent
import miles.scribble.dagger.activity.ActivityComponentBuilder
import miles.scribble.dagger.activity.ActivityModule
import miles.scribble.dagger.activity.ActivityScope
import miles.scribble.dagger.viewmodel.CanvasViewModelFactory
import miles.scribble.home.HomeActivity
import miles.scribble.home.drawing.DrawingCurve
import miles.scribble.home.viewmodel.HomeState
import miles.scribble.home.viewmodel.HomeViewModel
import miles.scribble.redux.core.SimpleStore
import miles.scribble.redux.core.Store

/**
 * Created by mbpeele on 6/30/17.
 */
/**
 * Created by mbpeele on 6/28/17.
 */
@Module
class HomeModule(activity: HomeActivity) : ActivityModule<HomeActivity>(activity) {

    @Provides
    @ActivityScope
    fun viewModel(factory: ViewModelProvider.Factory) : HomeViewModel {
        return ViewModelProviders.of(activity, factory)[HomeViewModel::class.java]
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