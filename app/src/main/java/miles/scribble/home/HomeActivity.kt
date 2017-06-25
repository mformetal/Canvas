package miles.scribble.home

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.PersistableBundle
import android.provider.MediaStore
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.NavigationView
import android.support.v4.app.ActivityCompat
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.view.MenuItem
import android.view.View
import butterknife.BindView
import butterknife.ButterKnife
import kotlinx.android.synthetic.main.activity_home.*

import java.io.File
import java.io.IOException

import miles.scribble.R
import miles.scribble.extensions.systemUIGone
import miles.scribble.extensions.systemUIVisibile
import miles.scribble.ui.BaseActivity
import miles.scribble.gallery.GalleryActivity
import miles.scribble.home.drawing.DrawingCurve
import miles.scribble.ui.transition.TransitionHelper
import miles.scribble.ui.widget.CanvasLayout
import miles.scribble.ui.widget.RoundedFrameLayout
import miles.scribble.util.FileUtils
import miles.scribble.util.ViewUtils


class HomeActivity : BaseActivity(), NavigationView.OnNavigationItemSelectedListener {

    private val TAG_FRAGMENT_COLOR_PICKER = "color"
    private val TAG_FRAGMENT_FILENAME = "name"
    private val TAG_FRAGMENT_BRUSH = "brush"
    private val TAG_FRAGMENT_TEXT = "text"
    private val REQUEST_IMPORT_CODE = 2001
    private val REQUEST_CAMERA_CODE = 2002
    private val REQUEST_PERMISSION_CAMERA_CODE = 2003

    @BindView(R.id.canvas) lateinit var canvasLayout: CanvasLayout
    @BindView(R.id.canvas_framelayout_animator) lateinit var fabFrame: RoundedFrameLayout

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        ButterKnife.bind(this)

        ViewUtils.systemUIGone(window.decorView)

        root.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
        navigation.setNavigationItemSelectedListener(this)

        root.addDrawerListener(object : DrawerLayout.SimpleDrawerListener() {
            override fun onDrawerOpened(drawerView: View?) {
                window.decorView.systemUIVisibile()
            }

            override fun onDrawerClosed(drawerView: View?) {
                window.decorView.systemUIGone()
            }
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                REQUEST_IMPORT_CODE -> {

                }
                REQUEST_CAMERA_CODE -> {

                }
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>,
                                            grantResults: IntArray) {
        when (requestCode) {
            REQUEST_PERMISSION_CAMERA_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                }
            }
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.activity_home_menu_gallery -> startActivity(Intent(this, GalleryActivity::class.java))
        }

        root.closeDrawer(GravityCompat.START)
        return true
    }
}
