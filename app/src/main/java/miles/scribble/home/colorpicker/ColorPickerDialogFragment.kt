package miles.scribble.home.colorpicker

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.view.WindowManager
import miles.redux.core.Dispatcher
import miles.scribble.R
import miles.scribble.home.viewmodel.HomeViewModel
import miles.scribble.ui.ViewModelDialogFragment
import miles.scribble.util.ViewUtils
import miles.scribble.util.extensions.getDisplaySize
import miles.scribble.util.extensions.hideKeyboard
import miles.scribble.util.extensions.inflater
import miles.scribble.util.extensions.isLandScape

/**
 * Created by mbpeele on 7/8/17.
 */
class ColorPickerDialogFragment : ViewModelDialogFragment<HomeViewModel>() {

    private val KEY_CURRENT_COLOR = "currentColor"
    private val KEY_TO_FILL = "fill"

    private lateinit var colorPicker : ColorPickerView
    lateinit var dispatcher : Dispatcher<ColorPickerEvents, ColorPickerEvents>

    companion object {
        @SuppressLint("NewApi")
        fun newInstance(toFill: Boolean) : ColorPickerDialogFragment {
            return ColorPickerDialogFragment().apply {
                arguments = Bundle().apply {
                    putBoolean(KEY_TO_FILL, toFill)
                }
            }
        }
    }

    override fun inject(): HomeViewModel {
        TODO()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        super.onCreateDialog(savedInstanceState)

        val view = activity.inflater().inflate(R.layout.color_picker_fragment, null, false).apply {
            colorPicker = findViewById(R.id.picker)
        }

        val toFill = arguments.getBoolean(KEY_TO_FILL)
        val color = savedInstanceState?.getInt(KEY_CURRENT_COLOR) ?:
                if (toFill) viewModel.state.backgroundColor else viewModel.state.paint.color
        colorPicker.setColor(color)

        return AlertDialog.Builder(activity)
                .setView(view)
                .setPositiveButton(R.string.positive_button, { dialog, _ ->
                    val chosenColor = colorPicker.viewModel.currentColor

                    val event = if (toFill) {
                        ColorPickerEvents.BackgroundColorChosen(chosenColor)
                    } else {
                        ColorPickerEvents.StrokeColorChosen(chosenColor)
                    }

                    dispatcher.dispatch(event)

                    ViewUtils.systemUIGone(activity.window.decorView)

                    dialog.dismiss()
                })
                .setNegativeButton(android.R.string.cancel, null)
                .create()
    }

    override fun onResume() {
        super.onResume()

        if (activity.isLandScape()) {
            val point = activity.getDisplaySize()
            val desiredWidth = (point.x * .9f).toInt()

            dialog.window.setLayout(desiredWidth, WindowManager.LayoutParams.MATCH_PARENT)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putInt(KEY_CURRENT_COLOR, colorPicker.viewModel.currentColor)
    }

    override fun onDismiss(dialog: DialogInterface?) {
        super.onDismiss(dialog)

        activity?.hideKeyboard()
    }
}