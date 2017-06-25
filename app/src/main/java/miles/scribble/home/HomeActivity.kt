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
import miles.scribble.ui.widget.CanvasLayout.CanvasLayoutListener
import miles.scribble.ui.widget.RoundedFrameLayout
import miles.scribble.util.FileUtils
import miles.scribble.util.ViewUtils


class HomeActivity : BaseActivity(), CanvasLayoutListener, NavigationView.OnNavigationItemSelectedListener {

    lateinit internal var canvasLayout: CanvasLayout
    lateinit private var fabFrame: RoundedFrameLayout

    private var filePath: String = ""

    private var count: Int = 0

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        ViewUtils.systemUIGone(window.decorView)

        canvasLayout = findViewById(R.id.canvas) as CanvasLayout
        fabFrame = findViewById(R.id.canvas_framelayout_animator) as RoundedFrameLayout

        root.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
        navigation.setNavigationItemSelectedListener(this)

        root.setDrawerListener(object : DrawerLayout.SimpleDrawerListener() {
            override fun onDrawerOpened(drawerView: View?) {
                window.decorView.systemUIVisibile()
            }

            override fun onDrawerClosed(drawerView: View?) {
                window.decorView.systemUIGone()
            }
        })

        canvasLayout.setActivityListener(this)
    }

    override fun onSaveInstanceState(outState: Bundle, outPersistentState: PersistableBundle) {
        super.onSaveInstanceState(outState, outPersistentState)
        outState.putString("filePath", filePath)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        filePath = savedInstanceState.getString("filePath")
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
                    showCamera()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        FileUtils.deleteTemporaryFiles(this)
    }

    override fun onFabMenuButtonClicked(view: View) {
        fabFrame = findViewById(R.id.canvas_framelayout_animator) as RoundedFrameLayout

        when (view.id) {
            R.id.menu_brush -> showBrushChooser(view)
            R.id.menu_stroke_color -> showColorChooser(view, false)
            R.id.menu_text -> showTextFragment(view)
            R.id.menu_upload -> showFilenameFragment(view)
            R.id.menu_canvas_color -> showColorChooser(view, true)
            R.id.menu_image -> showImageChooser()
        }
    }

    override fun onOptionsMenuButtonClicked(view: View, state: DrawingCurve.State) {
        when (state) {
            DrawingCurve.State.TEXT -> {
                if (view.id == R.id.view_options_menu_1) {
                    showTextFragment(view)
                } else {
                    showColorChooser(view, false)
                }
            }
            DrawingCurve.State.PICTURE -> {
                if (view.id == R.id.view_options_menu_1) {
                    showCamera()
                } else {
                    showGallery()
                }
            }
        }
    }

    override fun onNavigationIconClicked() {
        root.openDrawer(GravityCompat.START)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.activity_home_menu_gallery -> startActivity(Intent(this, GalleryActivity::class.java))
        }

        root.closeDrawer(GravityCompat.START)
        return true
    }

    private fun showBrushChooser(view: View) {
        val picker = BrushPickerFragment.newInstance(canvasLayout.paint)

        TransitionHelper.makeFabDialogTransitions(this, view, fabFrame, picker)

        supportFragmentManager.beginTransaction()
                .replace(R.id.canvas_framelayout_animator, picker, TAG_FRAGMENT_BRUSH)
                .commit()

        count++
    }

    private fun showColorChooser(view: View, toFill: Boolean) {
        val color = if (toFill) canvasLayout.backgroundColor else canvasLayout.brushColor
        val picker = ColorPickerFragment.newInstance(color, toFill)

        if (view is FloatingActionButton) {
            TransitionHelper.makeFabDialogTransitions(this, view, fabFrame, picker)
        } else {
            TransitionHelper.makeButtonDialogTransitions(this, view, fabFrame, picker)
        }

        supportFragmentManager.beginTransaction()
                .replace(R.id.canvas_framelayout_animator, picker, TAG_FRAGMENT_COLOR_PICKER)
                .commit()

        count++
    }

    private fun showTextFragment(view: View) {
        val text = TextFragment.newInstance()

        if (view is FloatingActionButton) {
            TransitionHelper.makeFabDialogTransitions(this, view, fabFrame, text)
        } else {
            TransitionHelper.makeButtonDialogTransitions(this, view, fabFrame, text)
        }

        supportFragmentManager.beginTransaction()
                .replace(R.id.canvas_framelayout_animator, text, TAG_FRAGMENT_TEXT)
                .commit()

        count++
    }

    private fun showFilenameFragment(view: View) {
        val filename = FilenameFragment.newInstance()

        TransitionHelper.makeFabDialogTransitions(this, view, fabFrame, filename)

        supportFragmentManager.beginTransaction()
                .replace(R.id.canvas_framelayout_animator, filename, TAG_FRAGMENT_FILENAME)
                .commit()

        count++
    }

    private fun showGallery() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(intent, REQUEST_IMPORT_CODE)
    }

    private fun showCamera() {
        val permissions = arrayOf(android.Manifest.permission.CAMERA, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
        if (checkPermissions(permissions)) {
            if (packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
                val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                if (takePictureIntent.resolveActivity(packageManager) != null) {
                    var photoFile: File? = null
                    try {
                        photoFile = FileUtils.createPhotoFile()
                        filePath = photoFile.absolutePath
                    } catch (e: IOException) {
                    }

                    if (photoFile != null) {
                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile))
                        startActivityForResult(takePictureIntent, REQUEST_CAMERA_CODE)
                    }
                }
            }
        } else {
            ActivityCompat.requestPermissions(this, permissions, REQUEST_PERMISSION_CAMERA_CODE)
        }
    }

    private fun showImageChooser() {
        val builder = Dialog(this)
        builder.setContentView(R.layout.dialog_image_chooser)
        builder.findViewById<View>(R.id.dialog_from_camera).setOnClickListener {
            builder.dismiss()
            showCamera()
        }
        builder.findViewById<View>(R.id.dialog_from_gallery).setOnClickListener {
            builder.dismiss()
            showGallery()
        }
        builder.show()
    }

    companion object {

        private val TAG_FRAGMENT_COLOR_PICKER = "color"
        private val TAG_FRAGMENT_FILENAME = "name"
        private val TAG_FRAGMENT_BRUSH = "brush"
        private val TAG_FRAGMENT_TEXT = "text"
        private val REQUEST_IMPORT_CODE = 2001
        private val REQUEST_CAMERA_CODE = 2002
        private val REQUEST_PERMISSION_CAMERA_CODE = 2003
    }
}
