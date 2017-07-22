package miles.scribble.home.di

import dagger.Module
import dagger.Provides
import dagger.Subcomponent
import miles.scribble.dagger.ViewScope
import miles.scribble.dagger.fragment.FragmentComponent
import miles.scribble.dagger.fragment.FragmentComponentBuilder
import miles.scribble.dagger.fragment.FragmentModule
import miles.scribble.dagger.fragment.FragmentScope
import miles.scribble.home.colorpicker.ColorPickerDialogFragment
import miles.scribble.home.colorpicker.ColorPickerEvents
import miles.scribble.home.colorpicker.ColorPickerReducer
import miles.scribble.home.events.CircleMenuEvents
import miles.scribble.home.events.CircleMenuEventsReducer
import miles.scribble.home.viewmodel.HomeState
import miles.scribble.home.viewmodel.HomeViewModel
import miles.scribble.redux.core.Dispatcher
import miles.scribble.redux.core.Dispatchers
import miles.scribble.redux.core.Reducer

/**
 * Created by mbpeele on 7/8/17.
 */
@FragmentScope
@Subcomponent(modules = arrayOf(ColorPickerModule::class))
interface ColorPickerComponent : FragmentComponent<ColorPickerDialogFragment> {

    fun viewModel() : HomeViewModel

    @Subcomponent.Builder
    interface Builder : FragmentComponentBuilder<ColorPickerModule, ColorPickerComponent>

}

@Module
class ColorPickerModule(fragment: ColorPickerDialogFragment) : FragmentModule<ColorPickerDialogFragment>(fragment) {

    @Provides
    @FragmentScope
    fun reducer() : Reducer<ColorPickerEvents, HomeState> {
        return ColorPickerReducer()
    }

    @Provides
    @FragmentScope
    fun dispatcher(homeViewModel: HomeViewModel, reducer: Reducer<ColorPickerEvents, HomeState>)
            : Dispatcher<ColorPickerEvents, ColorPickerEvents> {
        return Dispatchers.create(homeViewModel.store, reducer)
    }
}