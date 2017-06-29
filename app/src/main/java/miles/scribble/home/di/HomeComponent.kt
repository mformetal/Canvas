package miles.scribble.home.di

import dagger.Subcomponent
import miles.scribble.dagger.activity.ActivityComponent
import miles.scribble.dagger.activity.ActivityComponentBuilder
import miles.scribble.dagger.activity.ActivityScope
import miles.scribble.home.HomeActivity
import miles.scribble.home.viewmodel.HomeViewModel

/**
 * Created by mbpeele on 6/28/17.
 */
@ActivityScope
@Subcomponent(modules = arrayOf(HomeModule::class))
interface HomeComponent : ActivityComponent<HomeActivity> {

    fun viewModel() : HomeViewModel

    @Subcomponent.Builder
    interface Builder : ActivityComponentBuilder<HomeModule, HomeComponent>

}