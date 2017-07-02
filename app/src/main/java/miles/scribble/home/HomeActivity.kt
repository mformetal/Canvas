package miles.scribble.home

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.graphics.Point
import android.hardware.Sensor
import android.hardware.SensorManager
import android.net.Uri
import android.os.Bundle
import android.support.v4.widget.DrawerLayout
import android.view.OrientationEventListener
import android.view.View
import android.view.WindowManager
import butterknife.BindView
import butterknife.ButterKnife
import kotlinx.android.synthetic.main.activity_home.*
import miles.scribble.MainApp

import miles.scribble.R
import miles.scribble.dagger.activity.HasActivitySubcomponentBuilders
import miles.scribble.home.di.HomeComponent
import miles.scribble.home.di.HomeModule
import miles.scribble.home.viewmodel.HomeState
import miles.scribble.home.viewmodel.HomeViewModel
import miles.scribble.redux.core.Dispatcher
import miles.scribble.ui.ViewModelActivity
import miles.scribble.ui.widget.CanvasLayout
import miles.scribble.ui.widget.RoundedFrameLayout
import miles.scribble.util.ViewUtils
import miles.scribble.util.extensions.*
import javax.inject.Inject


class HomeActivity : ViewModelActivity<HomeViewModel>() {

    val REQUEST_PERMISSION_WRITE_SETTINGS = 1

    lateinit var component : HomeComponent

    override fun inject(app: MainApp) : HomeViewModel {
        val builder = app.getBuilder(HomeActivity::class.java)
        val componentBuilder = builder as HomeComponent.Builder
        component = componentBuilder.module(HomeModule(this)).build()
        component.injectMembers(this)
        return component.viewModel()
    }

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        ButterKnife.bind(this)
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
