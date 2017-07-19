package miles.scribble.home.colorpicker

import android.app.Dialog
import android.os.Bundle
import android.support.v7.app.AlertDialog
import butterknife.BindView
import butterknife.ButterKnife
import miles.scribble.R
import miles.scribble.dagger.fragment.HasFragmentSubcomponentBuilders
import miles.scribble.home.di.ColorPickerComponent
import miles.scribble.home.di.ColorPickerModule
import miles.scribble.home.viewmodel.HomeViewModel
import miles.scribble.ui.ViewModelDialogFragment
import miles.scribble.util.extensions.inflater

/**
 * Created by mbpeele on 7/8/17.
 */
class ColorPickerDialogFragment : ViewModelDialogFragment<HomeViewModel>() {

    @BindView(R.id.picker)
    lateinit var colorPicker : ColorPickerView

    override fun inject(hasFragmentSubcomponentBuilders: HasFragmentSubcomponentBuilders): HomeViewModel {
        val builder = hasFragmentSubcomponentBuilders.getBuilder(ColorPickerDialogFragment::class.java)
        val componentBuilder = builder as ColorPickerComponent.Builder
        val component = componentBuilder.module(ColorPickerModule(this)).build()
        return component.viewModel()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = activity.inflater().inflate(R.layout.color_picker_fragment, null, false).apply {
            ButterKnife.bind(this@ColorPickerDialogFragment, this)
        }

        return AlertDialog.Builder(activity)
                .setView(view)
                .setTitle(R.string.fragment_color_picker_title)
                .setPositiveButton(android.R.string.ok, null)
                .setNegativeButton(android.R.string.cancel, null)
                .create()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        colorPicker.setColor(viewModel.state.paint.color)
    }
}