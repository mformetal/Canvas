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
import android.net.Uri
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat.startActivity




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
