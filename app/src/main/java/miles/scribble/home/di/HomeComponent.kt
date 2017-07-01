package miles.scribble.home.di

import dagger.Subcomponent
import miles.scribble.dagger.activity.ActivityComponent
import miles.scribble.dagger.activity.ActivityComponentBuilder
import miles.scribble.dagger.activity.ActivityScope
import miles.scribble.home.HomeActivity
import miles.scribble.home.viewmodel.HomeViewModel
import miles.scribble.ui.widget.CanvasLayout
import miles.scribble.ui.widget.CanvasSurface
import miles.scribble.ui.widget.CircleFabMenu

/**
 * Created by mbpeele on 6/28/17.
 */
@ActivityScope
@Subcomponent(modules = arrayOf(HomeModule::class))
interface HomeComponent : ActivityComponent<HomeActivity> {

    fun viewModel() : HomeViewModel

    fun inject(canvasSurface: CanvasSurface)

    fun inject(canvasLayout: CanvasLayout)

    fun inject(circleFabMenu: CircleFabMenu)

    @Subcomponent.Builder
    interface Builder : ActivityComponentBuilder<HomeModule, HomeComponent>

}