package miles.scribble.ui.activity

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.os.Bundle
import android.os.PersistableBundle
import android.provider.MediaStore
import android.support.design.widget.NavigationView
import android.support.design.widget.Snackbar
import android.support.v4.app.ActivityCompat
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.view.MenuItem
import android.view.View
import kotlinx.android.synthetic.main.activity_home.*

import java.io.File
import java.io.IOException
import java.util.UUID

import miles.scribble.R
import miles.scribble.data.event.EventBitmapChosen
import miles.scribble.data.event.EventFilenameChosen
import miles.scribble.data.model.Sketch
import miles.scribble.ui.drawing.DrawingCurve
import miles.scribble.ui.fragment.BrushPickerFragment
import miles.scribble.ui.fragment.ColorPickerFragment
import miles.scribble.ui.fragment.FilenameFragment
import miles.scribble.ui.fragment.TextFragment
import miles.scribble.ui.transition.TransitionHelper
import miles.scribble.ui.widget.CanvasLayout
import miles.scribble.ui.widget.CanvasLayout.CanvasLayoutListener
import miles.scribble.ui.widget.Fab
import miles.scribble.ui.widget.RoundedFrameLayout
import miles.scribble.util.FileUtils
import miles.scribble.util.Logg
import miles.scribble.rx.SafeSubscription
import miles.scribble.util.ViewUtils
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers


class HomeActivity : BaseActivity(), CanvasLayoutListener, NavigationView.OnNavigationItemSelectedListener {

    lateinit internal var canvasLayout: CanvasLayout
    lateinit private var fabFrame: RoundedFrameLayout

    private var filePath: String = ""

    private var count: Int = 0

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        ViewUtils.systemUIGone(window.decorView)

        canvasLayout = findViewById(R.id.activity_home_canvas_root) as CanvasLayout
        fabFrame = findViewById(R.id.canvas_framelayout_animator) as RoundedFrameLayout

        activity_home_drawer_layout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
        activity_home_navigation.setNavigationItemSelectedListener(this)

        activity_home_drawer_layout.setDrawerListener(object : DrawerLayout.SimpleDrawerListener() {
            override fun onDrawerOpened(drawerView: View?) {
                ViewUtils.systemUIVisibile(window.decorView)
            }

            override fun onDrawerClosed(drawerView: View?) {
                ViewUtils.systemUIGone(window.decorView)
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
                    bus.post(EventBitmapChosen(data.data))
                }
                REQUEST_CAMERA_CODE -> {
                    val uri = FileUtils.addFileToGallery(this, filePath)
                    bus.post(EventBitmapChosen(uri))
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
        activity_home_drawer_layout.openDrawer(GravityCompat.START)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.activity_home_menu_gallery -> startActivity(Intent(this, GalleryActivity::class.java))
        }

        activity_home_drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }

    fun onEvent(eventFilenameChosen: EventFilenameChosen) {
        val saver = findViewById(R.id.menu_upload) as Fab
        val safeSubscription = object : SafeSubscription<ByteArray>(this) {
            override fun onError(e: Throwable) {
                super.onError(e)
                saver.stopSaveAnimation()
            }

            override fun onCompleted() {
                super.onCompleted()
                saver.stopSaveAnimation()
                showSnackbar(canvasLayout,
                        R.string.snackbar_activity_home_image_saved_title,
                        Snackbar.LENGTH_LONG, null)
            }

            override fun onNext(o: ByteArray) {
                super.onNext(o)
                realm.beginTransaction()
                val sketch = realm.createObject(Sketch::class.java)
                sketch.bytes = o
                sketch.title = eventFilenameChosen.filename
                sketch.id = UUID.randomUUID().toString()
                realm.commitTransaction()
            }

            override fun onStart() {
                super.onStart()
                saver.startSaveAnimation()
            }
        }

        val root = canvasLayout.drawerBitmap
        val bitmap = Bitmap.createBitmap(root.width, root.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(canvasLayout.backgroundColor)
        canvas.drawBitmap(root, 0f, 0f, null)

        FileUtils.compress(bitmap)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(safeSubscription)
    }

    private fun showBrushChooser(view: View) {
        val picker = BrushPickerFragment.newInstance(canvasLayout.paint)

        TransitionHelper.makeFabDialogTransitions(this, view, fabFrame, picker)

        fragmentManager.beginTransaction()
                .replace(R.id.canvas_framelayout_animator, picker, TAG_FRAGMENT_BRUSH)
                .commit()

        count++
    }

    private fun showColorChooser(view: View, toFill: Boolean) {
        val color = if (toFill) canvasLayout.backgroundColor else canvasLayout.brushColor
        val picker = ColorPickerFragment.newInstance(color, toFill)

        if (view is Fab) {
            TransitionHelper.makeFabDialogTransitions(this, view, fabFrame, picker)
        } else {
            TransitionHelper.makeButtonDialogTransitions(this, view, fabFrame, picker)
        }

        fragmentManager.beginTransaction()
                .replace(R.id.canvas_framelayout_animator, picker, TAG_FRAGMENT_COLOR_PICKER)
                .commit()

        count++
    }

    private fun showTextFragment(view: View) {
        val text = TextFragment.newInstance()

        if (view is Fab) {
            TransitionHelper.makeFabDialogTransitions(this, view, fabFrame, text)
        } else {
            TransitionHelper.makeButtonDialogTransitions(this, view, fabFrame, text)
        }

        fragmentManager.beginTransaction()
                .replace(R.id.canvas_framelayout_animator, text, TAG_FRAGMENT_TEXT)
                .commit()

        count++
    }

    private fun showFilenameFragment(view: View) {
        val filename = FilenameFragment.newInstance()

        TransitionHelper.makeFabDialogTransitions(this, view, fabFrame, filename)

        fragmentManager.beginTransaction()
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
                        Logg.log(e)
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
