package miles.scribble.home

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.support.v4.app.ActivityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.AlertDialog
import android.view.View
import butterknife.BindView
import butterknife.ButterKnife
import kotlinx.android.synthetic.main.activity_home.*

import miles.scribble.R
import miles.scribble.dagger.activity.HasActivitySubcomponentBuilders
import miles.scribble.home.di.HomeComponent
import miles.scribble.home.di.HomeModule
import miles.scribble.home.viewmodel.HomeViewModel
import miles.scribble.ui.ViewModelActivity
import miles.scribble.ui.widget.CanvasLayout
import miles.scribble.ui.widget.RoundedFrameLayout
import miles.scribble.util.ViewUtils
import miles.scribble.util.extensions.*


class HomeActivity : ViewModelActivity<HomeViewModel>() {

    val REQUEST_PERMISSION_WRITE_SETTINGS = 1

    @BindView(R.id.canvas) lateinit var canvasLayout: CanvasLayout
    @BindView(R.id.canvas_framelayout_animator) lateinit var fabFrame: RoundedFrameLayout

    override fun inject(hasActivitySubcomponentBuilders: HasActivitySubcomponentBuilders) : HomeViewModel {
        val builder = hasActivitySubcomponentBuilders.getBuilder(HomeActivity::class.java)
        val componentBuilder = builder as HomeComponent.Builder
        val component = componentBuilder.module(HomeModule(this)).build()
        component.injectMembers(this)
        return component.viewModel()
    }

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        ButterKnife.bind(this)

        root.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
        root.addDrawerListener(object : DrawerLayout.SimpleDrawerListener() {
            override fun onDrawerOpened(drawerView: View?) {
                window.decorView.systemUIVisibile()
            }

            override fun onDrawerClosed(drawerView: View?) {
                window.decorView.systemUIGone()
            }
        })
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
