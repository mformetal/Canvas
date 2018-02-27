package miles.scribble.home

import android.annotation.SuppressLint
import android.app.Activity
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.os.Bundle
import io.reactivex.disposables.Disposable
import mformetal.kodi.android.KodiActivity
import mformetal.kodi.core.Kodi
import mformetal.kodi.core.api.ScopeRegistry
import mformetal.kodi.core.api.builder.bind
import mformetal.kodi.core.api.builder.get
import mformetal.kodi.core.api.injection.register
import mformetal.kodi.core.api.scoped
import mformetal.kodi.core.provider.provider
import miles.dispatch.core.Dispatcher
import miles.dispatch.core.Dispatchers
import miles.dispatch.core.Store
import miles.scribble.R
import miles.scribble.home.brushpicker.BrushPickerDialogFragment
import miles.scribble.home.colorpicker.ColorPickerDialogFragment
import miles.scribble.home.events.CircleMenuEvents
import miles.scribble.home.events.HomeActivityEvents
import miles.scribble.home.events.HomeActivityEventsReducer
import miles.scribble.home.viewmodel.HomeState
import miles.scribble.home.viewmodel.HomeViewModel
import miles.scribble.home.viewmodel.HomeViewModelFactory
import miles.scribble.util.extensions.systemUIGone

class HomeActivity : KodiActivity() {

    private val DIALOG_COLOR_PICKER_STROKE = "strokeColorPicker"
    private val DIALOG_COLOR_PICKER_BACKGROUND = "backgroundColorPicker"
    private val DIALOG_BRUSH_PICKER = "brushPicker"

    private val REQUEST_IMAGE = 1

    private val viewModel : HomeViewModel by injector.register()
    private val dispatcher : Dispatcher<HomeActivityEvents, HomeActivityEvents> by injector.register()
    private lateinit var clickDispoable : Disposable

    override fun installModule(kodi: Kodi): ScopeRegistry {
        return kodi.scopeBuilder()
                .build(scoped<HomeActivity>()) {
                    bind<HomeViewModel>() using provider {
                        ViewModelProviders.of(this@HomeActivity, get<HomeViewModelFactory>())[HomeViewModel::class.java]
                    }
                    bind<Dispatcher<HomeActivityEvents, HomeActivityEvents>>() using provider {
                        Dispatchers.create(get(), HomeActivityEventsReducer())
                    }
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
                    val intent = Intent().apply {
                        type = "image/*"
                        action = Intent.ACTION_GET_CONTENT
                    }
                    startActivityForResult(intent, REQUEST_IMAGE)
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            REQUEST_IMAGE -> {
                if (resultCode == Activity.RESULT_OK) {
                    dispatcher.dispatch(HomeActivityEvents.PictureChosen(contentResolver, data!!.data))
                }
            }
            else -> {
                super.onActivityResult(requestCode, resultCode, data)
            }
        }
    }
}
