package miles.scribble.home.di

import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.Subcomponent
import dagger.multibindings.IntoMap
import miles.scribble.dagger.activity.*
import miles.scribble.dagger.fragment.FragmentComponentBuilder
import miles.scribble.dagger.fragment.FragmentKey
import miles.scribble.home.HomeActivity
import miles.scribble.home.brushpicker.BrushPickerDialogFragment
import miles.scribble.home.colorpicker.ColorPickerDialogFragment
import miles.scribble.home.viewmodel.HomeViewModel

/**
 * Created by mbpeele on 6/30/17.
 */
/**
 * Created by mbpeele on 6/28/17.
 */
@Module(includes = arrayOf(HomeActivityFragmentBindingModule::class))
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

@Module(subcomponents = arrayOf(ColorPickerComponent::class, BrushPickerComponent::class))
abstract class HomeActivityFragmentBindingModule {

    @Binds
    @IntoMap
    @FragmentKey(ColorPickerDialogFragment::class)
    abstract fun colorPickerBuilder(impl: ColorPickerComponent.Builder) : FragmentComponentBuilder<*, *>

    @Binds
    @IntoMap
    @FragmentKey(BrushPickerDialogFragment::class)
    abstract fun brushPickerBuilder(impl: BrushPickerComponent.Builder) : FragmentComponentBuilder<*, *>
}