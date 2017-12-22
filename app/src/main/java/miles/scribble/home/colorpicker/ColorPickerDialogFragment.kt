package miles.scribble.home.colorpicker

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.view.WindowManager
import mformetal.kodi.android.KodiDialogFragment
import mformetal.kodi.core.Kodi
import mformetal.kodi.core.api.ScopeRegistry
import mformetal.kodi.core.api.builder.bind
import mformetal.kodi.core.api.builder.get
import mformetal.kodi.core.api.injection.register
import mformetal.kodi.core.api.scoped
import mformetal.kodi.core.provider.provider
import miles.dispatch.core.Dispatcher
import miles.dispatch.core.Dispatchers
import miles.scribble.R
import miles.scribble.home.HomeActivity
import miles.scribble.home.events.ColorPickerEvents
import miles.scribble.home.events.ColorPickerReducer
import miles.scribble.home.viewmodel.HomeViewModel
import miles.scribble.util.extensions.*

/**
 * Created from mbpeele on 7/8/17.
 */
class ColorPickerDialogFragment : KodiDialogFragment() {

    private val KEY_TO_FILL = "fill"

    private lateinit var colorPicker : ColorPickerView
    val dispatcher : Dispatcher<ColorPickerEvents, ColorPickerEvents> by injector.register()
    val viewModel : HomeViewModel by injector.register()

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

    override fun installModule(kodi: Kodi): ScopeRegistry {
        return kodi.scopeBuilder()
                .dependsOn(scoped<HomeActivity>())
                .build(scoped<ColorPickerDialogFragment>()) {
                    bind<Dispatcher<ColorPickerEvents, ColorPickerEvents>>() using provider {
                        Dispatchers.create(get(), ColorPickerReducer())
                    }
                }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        super.onCreateDialog(savedInstanceState)

        val view = safeActivity.inflater().inflate(R.layout.color_picker_parent, null, false).apply {
            colorPicker = findViewById(R.id.picker)
        }

        val toFill = arguments!!.getBoolean(KEY_TO_FILL)
        val color = if (toFill) viewModel.state.backgroundColor else viewModel.state.paint.color
        colorPicker.setColor(color)

        return AlertDialog.Builder(safeActivity)
                .setView(view)
                .setPositiveButton(R.string.positive_button, { dialog, _ ->
                    val chosenColor = colorPicker.viewModel.currentColor

                    val event = if (toFill) {
                        ColorPickerEvents.BackgroundColorChosen(chosenColor)
                    } else {
                        ColorPickerEvents.StrokeColorChosen(chosenColor)
                    }

                    dispatcher.dispatch(event)

                    safeActivity.window.decorView.systemUIGone()

                    dialog.dismiss()
                })
                .setNegativeButton(android.R.string.cancel, null)
                .create()
    }

    override fun onResume() {
        super.onResume()

        if (safeActivity.isLandScape()) {
            val point = safeActivity.getDisplaySize()
            val desiredWidth = (point.x * .9f).toInt()

            dialog.window.setLayout(desiredWidth, WindowManager.LayoutParams.MATCH_PARENT)
        }
    }

    override fun onDismiss(dialog: DialogInterface?) {
        super.onDismiss(dialog)

        activity?.hideKeyboard()
    }
}