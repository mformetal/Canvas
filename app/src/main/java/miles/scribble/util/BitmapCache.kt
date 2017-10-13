package miles.scribble.util

import android.app.ActivityManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Point
import android.net.Uri
import com.bumptech.glide.util.LruCache

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.lang.ref.SoftReference
import java.util.Collections
import java.util.HashSet

import android.content.Context.ACTIVITY_SERVICE
import android.content.pm.ApplicationInfo.FLAG_LARGE_HEAP
import android.view.WindowManager

/**
 * Created from mbpeele on 1/5/16.
 */
private fun getMaxSize(context: Context): Int {
    val am = context.getSystemService(ACTIVITY_SERVICE) as ActivityManager
    val largeHeap = context.applicationInfo.flags and FLAG_LARGE_HEAP != 0
    var memoryClass = am.memoryClass
    if (largeHeap) {
        memoryClass = am.largeMemoryClass
    }
    return 1024 * 1024 * memoryClass / 5
}

internal class BitmapCache(context: Context) : LruCache<Uri, Bitmap>(getMaxSize(context)) {

    val reusableBitmaps: MutableSet<SoftReference<Bitmap>> = Collections.synchronizedSet(HashSet<SoftReference<Bitmap>>())
    val viewWidth: Int
    val viewHeight: Int

    init {
        val size = Point().apply {
            (context.getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay.getSize(this)
        }
        viewWidth = size.x
        viewHeight = size.y
    }

    override fun onItemEvicted(key: Uri?, item: Bitmap?) {
        super.onItemEvicted(key, item)
        reusableBitmaps.add(SoftReference<Bitmap>(item))
    }

    @Throws(IOException::class)
    fun decodeStream(inputStream: InputStream): Bitmap {
        val out = ByteArrayOutputStream()
        val buf = ByteArray(8191)
        var total: Long = 0
        while (true) {
            val r = inputStream.read(buf)
            if (r == -1) {
                break
            }
            out.write(buf, 0, r)
            total += r.toLong()
        }
        val `in` = ByteArrayInputStream(out.toByteArray())

        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        options.inMutable = true
        options.inPreferredConfig = Bitmap.Config.ARGB_8888

        BitmapFactory.decodeStream(`in`, null, options)

        `in`.reset()

        options.inSampleSize = calculateInSampleSize(options, viewWidth, viewHeight)

        addInBitmapOptions(options)

        options.inJustDecodeBounds = false

        return BitmapFactory.decodeStream(`in`, null, options)
    }

    private fun addInBitmapOptions(options: BitmapFactory.Options) {
        val inBitmap = getBitmapFromReusableSet(options)

        if (inBitmap != null) {
            options.inBitmap = inBitmap
        }
    }

    private fun getBitmapFromReusableSet(options: BitmapFactory.Options): Bitmap? {
        var bitmap: Bitmap? = null

        if (reusableBitmaps.isNotEmpty()) {
            synchronized(reusableBitmaps) {
                val iterator = reusableBitmaps.iterator()
                var item: Bitmap?

                while (iterator.hasNext()) {
                    item = iterator.next().get()

                    if (null != item && item.isMutable) {
                        if (canUseForInBitmap(item, options)) {
                            bitmap = item

                            iterator.remove()
                            break
                        }
                    } else {
                        iterator.remove()
                    }
                }
            }
        }
        return bitmap
    }

    private fun canUseForInBitmap(candidate: Bitmap, targetOptions: BitmapFactory.Options): Boolean {
        val width = targetOptions.outWidth / targetOptions.inSampleSize
        val height = targetOptions.outHeight / targetOptions.inSampleSize
        val byteCount = width * height * getBytesPerPixel(candidate.config)
        return byteCount <= candidate.allocationByteCount
    }

    private fun getBytesPerPixel(config: Bitmap.Config): Int {
        return when (config) {
            Bitmap.Config.ARGB_8888 -> 4
            Bitmap.Config.RGB_565 -> 2
            Bitmap.Config.ARGB_4444 -> 2
            Bitmap.Config.ALPHA_8 -> 1
            else -> 1
        }
    }

    private fun calculateInSampleSize(options: BitmapFactory.Options,
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
