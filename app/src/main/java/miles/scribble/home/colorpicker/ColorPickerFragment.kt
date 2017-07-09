package miles.scribble.home.colorpicker

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import butterknife.BindView
import butterknife.ButterKnife
import miles.scribble.R
import miles.scribble.dagger.fragment.HasFragmentSubcomponentBuilders
import miles.scribble.home.HomeActivity
import miles.scribble.home.di.ColorPickerComponent
import miles.scribble.home.di.ColorPickerModule
import miles.scribble.home.di.HomeComponent
import miles.scribble.home.di.HomeModule
import miles.scribble.home.viewmodel.HomeViewModel
import miles.scribble.ui.ViewModelFragment
import miles.scribble.util.extensions.inflater

/**
 * Created by mbpeele on 7/8/17.
 */
class ColorPickerFragment : ViewModelFragment<HomeViewModel>() {

    @BindView(R.id.picker)
    lateinit var colorPicker : ColorPickerView

    override fun inject(hasFragmentSubcomponentBuilders: HasFragmentSubcomponentBuilders): HomeViewModel {
        val builder = hasFragmentSubcomponentBuilders.getBuilder(ColorPickerFragment::class.java)
        val componentBuilder = builder as ColorPickerComponent.Builder
        val component = componentBuilder.module(ColorPickerModule(this)).build()
        return component.viewModel()
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return activity.inflater().inflate(R.layout.color_picker_fragment, container, false).apply {
            ButterKnife.bind(this@ColorPickerFragment, this)
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        colorPicker.setColor(viewModel.state.paint.color)
    }
}