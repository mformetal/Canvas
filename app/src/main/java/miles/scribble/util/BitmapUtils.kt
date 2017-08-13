package miles.scribble.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import miles.scribble.util.extensions.getDisplaySize
import java.io.FileNotFoundException

import java.io.IOException
import java.io.InputStream

/**
 * Created by Miles Peele on 9/23/2015.
 */
object BitmapUtils {

    fun calculateInSampleSize(options: BitmapFactory.Options,
                              reqWidth: Int, reqHeight: Int): Int {
        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {

            val halfHeight = height / 2
            val halfWidth = width / 2

            while (halfHeight / inSampleSize > reqHeight && halfWidth / inSampleSize > reqWidth) {
                inSampleSize *= 2
            }
        }

        return inSampleSize
    }
}