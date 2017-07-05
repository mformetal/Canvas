package miles.scribble.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import miles.scribble.util.extensions.getDisplaySize

import java.io.IOException
import java.io.InputStream

/**
 * Created by Miles Peele on 9/23/2015.
 */
object FileUtils {

    val DRAWING_BITMAP_FILENAME = "workerCanvas:workerBitmap"

    val bitmapOptions: BitmapFactory.Options
        get() {
            return BitmapFactory.Options().apply {
                inPreferredConfig = Bitmap.Config.ARGB_8888
                inMutable = true
                inPreferQualityOverSpeed = true
            }
        }

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

    fun getCachedBitmap(context: Context): Bitmap {
        var bitmap : Bitmap ?= null
        try {
            val inputStream = context.openFileInput(DRAWING_BITMAP_FILENAME)
            bitmap = BitmapFactory.decodeStream(inputStream, null, bitmapOptions)
            inputStream.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        if (bitmap == null) {
            val size = context.getDisplaySize()
            bitmap = Bitmap.createBitmap(size.x, size.y, Bitmap.Config.ARGB_8888).apply {
                eraseColor(Color.WHITE)
            }
        }

        return bitmap!!
    }
}
