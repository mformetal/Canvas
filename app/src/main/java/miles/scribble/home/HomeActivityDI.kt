package miles.scribble.home

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
import miles.scribble.home.brushpicker.BrushPickerDialogFragment
import miles.scribble.home.colorpicker.ColorPickerDialogFragment
import miles.scribble.home.brushpicker.BrushPickerComponent
import miles.scribble.home.colorpicker.ColorPickerComponent
import miles.scribble.home.di.*
import miles.scribble.home.viewmodel.HomeState
import miles.scribble.home.viewmodel.HomeViewModel
import miles.scribble.redux.core.Dispatcher
import miles.scribble.redux.core.Dispatchers
import miles.scribble.redux.core.Reducer

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

    @Provides
    @ActivityScope
    fun dispatcher(homeViewModel: HomeViewModel, reducer: Reducer<HomeActivityEvents, HomeState>) : Dispatcher<HomeActivityEvents, HomeActivityEvents> {
        return Dispatchers.create(homeViewModel.store, reducer)
    }

    @Provides
    @ActivityScope
    fun reducer() : Reducer<HomeActivityEvents, HomeState> {
        return HomeActivityEventsReducer()
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