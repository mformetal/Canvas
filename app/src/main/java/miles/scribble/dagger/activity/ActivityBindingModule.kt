package miles.scribble.dagger.activity;

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import miles.scribble.home.HomeActivity
import miles.scribble.home.di.HomeComponent

@Module(
        subcomponents = arrayOf(
                HomeComponent::class)
)
abstract class ActivityBindingModule {

    @Binds
    @IntoMap
    @ActivityKey(HomeActivity::class)
    abstract fun loginComponentBuilder(impl: HomeComponent.Builder) : ActivityComponentBuilder<*, *>

}