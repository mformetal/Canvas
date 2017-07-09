package miles.scribble.home.di

import dagger.Module
import dagger.Subcomponent
import miles.scribble.dagger.fragment.FragmentComponent
import miles.scribble.dagger.fragment.FragmentComponentBuilder
import miles.scribble.dagger.fragment.FragmentModule
import miles.scribble.dagger.fragment.FragmentScope
import miles.scribble.home.colorpicker.ColorPickerFragment
import miles.scribble.home.viewmodel.HomeViewModel

/**
 * Created by mbpeele on 7/8/17.
 */
@FragmentScope
@Subcomponent(modules = arrayOf(ColorPickerModule::class))
interface ColorPickerComponent : FragmentComponent<ColorPickerFragment> {

    fun viewModel() : HomeViewModel

    @Subcomponent.Builder
    interface Builder : FragmentComponentBuilder<ColorPickerModule, ColorPickerComponent>

}

@Module
class ColorPickerModule(fragment: ColorPickerFragment) : FragmentModule<ColorPickerFragment>(fragment)