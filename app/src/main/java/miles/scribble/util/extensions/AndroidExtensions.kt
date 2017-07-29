package miles.scribble.util.extensions

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.PendingIntent.getActivity
import android.content.Context
import android.os.Build
import android.provider.Settings
import miles.scribble.MainApp
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Point
import android.net.Uri
import android.support.design.widget.Snackbar
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat.startActivity
import android.view.LayoutInflater
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.support.design.widget.CoordinatorLayout
import android.R.attr.y
import android.app.ActionBar
import android.view.ViewGroup


/**
 * Created by mbpeele on 6/28/17.
 */
fun Context.app() = applicationContext as MainApp

fun Context.isAtLeastMarshmallow() : Boolean {
    return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
}

fun Context.hasWriteSettingsPermission() : Boolean {
    return if (isAtLeastMarshmallow()) {
        Settings.System.canWrite(this)
    } else {
        ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_SETTINGS) ==
                PackageManager.PERMISSION_GRANTED
    }
}

fun Activity.setAutoRotate(enable: Boolean) {
    if (hasWriteSettingsPermission()) {
        Settings.System.putInt(contentResolver, Settings.System.ACCELEROMETER_ROTATION, if (enable) 1 else 0)
    }
}

fun Activity.inflater() : LayoutInflater {
    return LayoutInflater.from(this)
}

fun Context.getDisplaySize() : Point {
    return Point().apply {
        (getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay.getRealSize(this)
    }
}

fun Context.getAppScreenSize() : Point {
    return Point().apply {
        (getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay.getSize(this)
    }
}

fun Activity.isLandScape() : Boolean  {
    return resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
}

fun Activity.hideKeyboard() {
    val imm = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    if (imm.isAcceptingText) {
        currentFocus?.let {
            it.clearFocus()
            imm.hideSoftInputFromWindow(it.windowToken, 0)
        }
    }
}

fun Context.navigationBarSize() : Int? {
    val screenSize = getDisplaySize()
    val appUsableSize = getAppScreenSize()
    if (appUsableSize.x < screenSize.x) {
        return appUsableSize.y
    }

    if (appUsableSize.y < screenSize.y) {
        return screenSize.y - appUsableSize.y
    }

    return null
}

fun Snackbar.adjustImmersiveHeight() : Snackbar {
    context.navigationBarSize()?.let {
        val parentParams = view.layoutParams as ViewGroup.MarginLayoutParams
        parentParams.setMargins(0, 0, 0, (0 - it))
        view.layoutParams = parentParams
        return this
    }

    return this
}

fun Canvas.drawBitmap(bitmap: Bitmap) {
    drawBitmap(bitmap, 0f, 0f, null)
}
