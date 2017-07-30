package miles.scribble.home

import android.content.Intent
import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import butterknife.ButterKnife
import io.reactivex.disposables.Disposable
import miles.scribble.MainApp

import miles.scribble.R
import miles.scribble.dagger.fragment.FragmentComponentBuilder
import miles.scribble.dagger.fragment.HasFragmentSubcomponentBuilders
import miles.scribble.home.brushpicker.BrushPickerDialogFragment
import miles.scribble.home.colorpicker.ColorPickerDialogFragment
import miles.scribble.home.di.HomeComponent
import miles.scribble.home.di.HomeModule
import miles.scribble.home.events.CircleMenuEvents
import miles.scribble.home.viewmodel.HomeViewModel
import miles.scribble.ui.ViewModelActivity
import miles.scribble.util.ViewUtils
import miles.scribble.util.extensions.*
import javax.inject.Inject
import javax.inject.Provider


class HomeActivity : ViewModelActivity<HomeViewModel>(), HasFragmentSubcomponentBuilders {

    private val DIALOG_COLOR_PICKER_STROKE = "strokeColorPicker"
    private val DIALOG_COLOR_PICKER_BACKGROUND = "backgroundColorPicker"
    private val DIALOG_BRUSH_PICKER = "brushPicker"

    private val REQUEST_PERMISSION_WRITE_SETTINGS = 1

    lateinit var component : HomeComponent
    @Inject
    lateinit var fragmentComponentBuilders: Map<Class<out Fragment>, @JvmSuppressWildcards Provider<FragmentComponentBuilder<*, *>>>

    lateinit var clickDispoable : Disposable

    override fun inject(app: MainApp) : HomeViewModel {
        val builder = app.getBuilder(HomeActivity::class.java)
        val componentBuilder = builder as HomeComponent.Builder
        component = componentBuilder.module(HomeModule(this)).build()
        component.injectMembers(this)
        return component.viewModel()
    }

    override fun getBuilder(fragmentClass: Class<out Fragment>): FragmentComponentBuilder<*, *> {
        return fragmentComponentBuilders.get(fragmentClass)!!.get()
    }

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        ButterKnife.bind(this)

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
            }
        }
    }

    override fun onPause() {
        super.onPause()

        viewModel.persistDrawings()
    }

    override fun onDestroy() {
        super.onDestroy()

        clickDispoable.dispose()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            REQUEST_PERMISSION_WRITE_SETTINGS -> {
                setAutoRotate(true)
            }
            else -> {
                super.onActivityResult(requestCode, resultCode, data)
            }
        }
    }

    override fun onResume() {
        super.onResume()

        ViewUtils.systemUIGone(window.decorView)

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
