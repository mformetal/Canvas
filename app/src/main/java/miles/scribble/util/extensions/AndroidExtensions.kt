package miles.scribble.util.extensions

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Point
import android.os.Build
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import miles.kodi.Kodi
import miles.scribble.App


/**
 * Created from mbpeele on 6/28/17.
 */
val Context.app : App get() = applicationContext as App
val Fragment.app : App get() = activity.app
val Context.kodi : Kodi get() = app.kodi

fun Context.isAtLeastMarshmallow() : Boolean {
    return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
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

fun Matrix.identity() : Matrix {
    val values = floatArrayOf(1f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 1f)
    setValues(values)
    return this
}

fun MotionEvent.distance() : Float {
    val dx = getX(0) - getX(1)
    val dy = getY(0) - getY(1)
    return Math.sqrt((dx * dx + dy * dy).toDouble()).toFloat()
}

fun MotionEvent.angle() : Float {
    val dx = getX(0) - getX(1)
    val dy = getY(0) - getY(1)
    val radians = Math.atan2(dy.toDouble(), dx.toDouble())
    return Math.toDegrees(radians).toFloat()
}
