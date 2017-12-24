package miles.scribble.home

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import io.reactivex.disposables.Disposable
import mformetal.kodi.android.KodiActivity
import mformetal.kodi.core.Kodi
import mformetal.kodi.core.api.ScopeRegistry
import mformetal.kodi.core.api.builder.bind
import mformetal.kodi.core.api.builder.get
import mformetal.kodi.core.api.injection.register
import mformetal.kodi.core.api.scoped
import mformetal.kodi.core.provider.provider
import miles.dispatch.core.Store
import miles.scribble.R
import miles.scribble.home.brushpicker.BrushPickerDialogFragment
import miles.scribble.home.choosepicture.ChoosePictureFragment
import miles.scribble.home.colorpicker.ColorPickerDialogFragment
import miles.scribble.home.events.CircleMenuEvents
import miles.scribble.home.viewmodel.HomeState
import miles.scribble.home.viewmodel.HomeViewModel
import miles.scribble.util.extensions.systemUIGone

class HomeActivity : KodiActivity() {

    private val DIALOG_COLOR_PICKER_STROKE = "strokeColorPicker"
    private val DIALOG_COLOR_PICKER_BACKGROUND = "backgroundColorPicker"
    private val DIALOG_BRUSH_PICKER = "brushPicker"

    private val REQUEST_EXTERNAL_STORAGE_PERMISSION = 1

    private val viewModel : HomeViewModel by injector.register()
    private lateinit var clickDispoable : Disposable

    override fun installModule(kodi: Kodi): ScopeRegistry {
        return kodi.scopeBuilder()
                .build(scoped<HomeActivity>()) {
                    bind<Store<HomeState>>() using provider { get<HomeViewModel>().store }
                }
    }

    @SuppressLint("MissingSuperCall")
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.home)

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
                    val hasPermission = ActivityCompat.checkSelfPermission(this,
                            Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                    if (hasPermission) {
                        supportFragmentManager.beginTransaction()
                                .add(R.id.canvas_layout, ChoosePictureFragment.newInstance())
                                .commit()
                    } else {
                        ActivityCompat.requestPermissions(this,
                                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                                REQUEST_EXTERNAL_STORAGE_PERMISSION)
                    }
                }
            }
        }
    }

    override fun onBackPressed() {
        val choosePictureFragment = supportFragmentManager.findFragmentById(R.id.canvas_layout)
        if (choosePictureFragment == null) {
            super.onBackPressed()
        } else {
            supportFragmentManager.beginTransaction()
                    .remove(choosePictureFragment)
                    .commit()
        }
    }

    override fun onResume() {
        super.onResume()

        window.decorView.systemUIGone()
    }

    override fun onPause() {
        super.onPause()

        viewModel.cacheDrawing()
    }

    @SuppressLint("MissingSuperCall")
    override fun onDestroy() {
        super.onDestroy()

        clickDispoable.dispose()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == REQUEST_EXTERNAL_STORAGE_PERMISSION) {
            val permissionIsGranted = grantResults[0] == PackageManager.PERMISSION_GRANTED
            if (permissionIsGranted) {
                supportFragmentManager.beginTransaction()
                        .add(R.id.canvas_layout, ChoosePictureFragment.newInstance())
                        .commitAllowingStateLoss()
            }
        }
    }
}
