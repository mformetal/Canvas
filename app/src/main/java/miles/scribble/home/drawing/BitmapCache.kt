package miles.scribble.home.drawing

import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Point
import android.net.Uri
import com.bumptech.glide.util.LruCache
import com.google.common.io.ByteStreams
import miles.scribble.util.FileUtils

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.lang.ref.SoftReference
import java.util.Collections
import java.util.HashSet

import android.content.Context.ACTIVITY_SERVICE
import android.content.pm.ApplicationInfo.FLAG_LARGE_HEAP

/**
 * Created by mbpeele on 1/5/16.
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

internal class BitmapCache(mContext: Context) : LruCache<Uri, Bitmap>(getMaxSize(mContext)) {

    val mReusableBitmaps: MutableSet<SoftReference<Bitmap>> = Collections.synchronizedSet(HashSet<SoftReference<Bitmap>>())
    val mViewWidth: Int
    val mViewHeight: Int

    init {
        val size = Point().apply {
            (mContext as Activity).windowManager.defaultDisplay.getSize(this)
        }
        mViewWidth = size.x
        mViewHeight = size.y
    }

    override fun onItemEvicted(key: Uri?, item: Bitmap?) {
        super.onItemEvicted(key, item)
        mReusableBitmaps.add(SoftReference<Bitmap>(item))
    }

    @Throws(IOException::class)
    fun decodeStream(inputStream: InputStream): Bitmap {
        val out = ByteArrayOutputStream()
        ByteStreams.copy(inputStream, out)
        val `in` = ByteArrayInputStream(out.toByteArray())

        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        options.inMutable = true
        options.inPreferredConfig = Bitmap.Config.ARGB_8888

        BitmapFactory.decodeStream(`in`, null, options)

        `in`.reset()

        options.inSampleSize = FileUtils.calculateInSampleSize(options, mViewWidth, mViewHeight)

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

        if (mReusableBitmaps.isNotEmpty()) {
            synchronized(mReusableBitmaps) {
                val iterator = mReusableBitmaps.iterator()
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

    private fun canUseForInBitmap(
            candidate: Bitmap, targetOptions: BitmapFactory.Options): Boolean {
        val width = targetOptions.outWidth / targetOptions.inSampleSize
        val height = targetOptions.outHeight / targetOptions.inSampleSize
        val byteCount = width * height * getBytesPerPixel(candidate.config)
        return byteCount <= candidate.allocationByteCount
    }

    private fun getBytesPerPixel(config: Bitmap.Config): Int {
        if (config == Bitmap.Config.ARGB_8888) {
            return 4
        } else if (config == Bitmap.Config.RGB_565) {
            return 2
        } else if (config == Bitmap.Config.ARGB_4444) {
            return 2
        } else if (config == Bitmap.Config.ALPHA_8) {
            return 1
        }
        return 1
    }
}
