package miles.scribble.home

import android.content.Intent
import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Bundle
import io.reactivex.disposables.Disposable
import miles.kodi.Kodi
import miles.kodi.api.ScopeRegistry
import miles.kodi.api.builder.bind
import miles.kodi.api.builder.get
import miles.kodi.api.injection.register
import miles.kodi.api.scoped
import miles.kodi.provider.provider
import miles.redux.core.Store
import miles.scribble.R
import miles.scribble.home.brushpicker.BrushPickerDialogFragment
import miles.scribble.home.choosepicture.ChoosePictureFragment
import miles.scribble.home.colorpicker.ColorPickerDialogFragment
import miles.scribble.home.events.CircleMenuEvents
import miles.scribble.home.viewmodel.HomeState
import miles.scribble.home.viewmodel.HomeViewModel
import miles.scribble.ui.KodiActivity
import miles.scribble.util.ViewUtils
import miles.scribble.util.extensions.hasWriteSettingsPermission
import miles.scribble.util.extensions.isAtLeastMarshmallow
import miles.scribble.util.extensions.setAutoRotate


class HomeActivity : KodiActivity() {

    private val DIALOG_COLOR_PICKER_STROKE = "strokeColorPicker"
    private val DIALOG_COLOR_PICKER_BACKGROUND = "backgroundColorPicker"
    private val DIALOG_BRUSH_PICKER = "brushPicker"

    private val REQUEST_PERMISSION_WRITE_SETTINGS = 1
    private val REQUEST_IMPORT_CODE = 2

    val viewModel : HomeViewModel by injector.register()
    lateinit var clickDispoable : Disposable

    override fun installModule(kodi: Kodi): ScopeRegistry {
        return kodi.scope {
            build(scoped<HomeActivity>()) {
                bind<Store<HomeState>>() using provider { get<HomeViewModel>().store }
            }
        }
    }

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        clickDispoable = viewModel.state.onClickSubject.subscribe {
            when (it) {
                is CircleMenuEvents.StrokeColorClicked -> {
                    ColorPickerDialogFragment.newInstance(false)
                            .show(supportFragmentManager, DIALOG_COLOR_PICKER_STROKE)
                }
                is CircleMenuEvents.BackgroundColorClicked -> {
                    ColorPickerDialogFragment.newInstance(true)
                            .show(supportFragmentManager, DIALOG_COLOR_PICKER_BACKGROUND)
                }
                is CircleMenuEvents.BrushClicked -> {
                    BrushPickerDialogFragment()
                            .show(supportFragmentManager, DIALOG_BRUSH_PICKER)
                }
                is CircleMenuEvents.PictureClicked -> {
                    val intent = Intent().apply {
                        type = "image/*"
                        action = Intent.ACTION_GET_CONTENT
                    }
                    startActivityForResult(intent, REQUEST_IMPORT_CODE)
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()

        viewModel.cacheDrawing()
    }

    override fun onDestroy() {
        super.onDestroy()

        clickDispoable.dispose()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            REQUEST_PERMISSION_WRITE_SETTINGS -> {
                setAutoRotate(true)
            }
            REQUEST_IMPORT_CODE -> {
                data?.data?.let {
                    supportFragmentManager.beginTransaction()
                            .add(R.id.canvas_layout, ChoosePictureFragment.newInstance(it))
                            .commit()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()

        ViewUtils.hideSystemUI(window.decorView)

        if (hasWriteSettingsPermission()) {
            setAutoRotate(true)
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR
        } else {
            if (isAtLeastMarshmallow()) {
                val intent = Intent(android.provider.Settings.ACTION_MANAGE_WRITE_SETTINGS)
                intent.data = Uri.parse("package:" + packageName)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
        }
    }

    override fun onStop() {
        super.onStop()

        setAutoRotate(false)
    }
}
