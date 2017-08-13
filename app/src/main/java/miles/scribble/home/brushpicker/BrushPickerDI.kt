package miles.scribble.home.brushpicker

import dagger.Module
import dagger.Provides
import dagger.Subcomponent
import miles.scribble.dagger.fragment.FragmentComponent
import miles.scribble.dagger.fragment.FragmentComponentBuilder
import miles.scribble.dagger.fragment.FragmentModule
import miles.scribble.dagger.fragment.FragmentScope
import miles.scribble.home.viewmodel.HomeState
import miles.scribble.home.viewmodel.HomeViewModel
import miles.scribble.redux.core.Dispatcher
import miles.scribble.redux.core.Dispatchers
import miles.scribble.redux.core.Reducer

/**
 * Created by mbpeele on 7/29/17.
 */
@FragmentScope
@Subcomponent(modules = arrayOf(BrushPickerModule::class))
interface BrushPickerComponent : FragmentComponent<BrushPickerDialogFragment> {

    fun viewModel() : HomeViewModel

    @Subcomponent.Builder
    interface Builder : FragmentComponentBuilder<BrushPickerModule, BrushPickerComponent>

}

@Module
class BrushPickerModule(fragment: BrushPickerDialogFragment) : FragmentModule<BrushPickerDialogFragment>(fragment) {

    @Provides
    @FragmentScope
    fun reducer() : Reducer<BrushPickerEvents, HomeState> {
        return BrushPickerReducer()
    }

    @Provides
    @FragmentScope
    fun dispatcher(homeViewModel: HomeViewModel, reducer: Reducer<BrushPickerEvents, HomeState>)
            : Dispatcher<BrushPickerEvents, BrushPickerEvents> {
        return Dispatchers.create(homeViewModel.store, reducer)
    }
}