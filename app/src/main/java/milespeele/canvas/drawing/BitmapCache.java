package milespeele.canvas.drawing;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;

import com.squareup.picasso.Cache;
import com.squareup.picasso.LruCache;

import java.lang.ref.SoftReference;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import milespeele.canvas.util.FileUtils;
import milespeele.canvas.util.Logg;

/**
 * Created by mbpeele on 1/4/16.
 */
class BitmapCache extends LruCache {

    private final Set<SoftReference<Bitmap>> mReusableBitmaps;
    public int count = 0;

    public BitmapCache(Context context) {
        super(context);
        mReusableBitmaps = Collections.synchronizedSet(new HashSet<>());
    }

    public void set(Bitmap bitmap) {
        String val = String.valueOf(count);
        mReusableBitmaps.add(new SoftReference<>(bitmap));
        super.set(val, bitmap);
        count++;
    }

    public Bitmap decode(Context context, String path) {
        BitmapFactory.Options options = FileUtils.getBitmapOptions(context);
        BitmapFactory.decodeFile(path, options);

        addInBitmapOptions(options);

        return BitmapFactory.decodeFile(path, options);
    }

    private void addInBitmapOptions(BitmapFactory.Options options) {
        options.inMutable = true;

        Bitmap inBitmap = getBitmapFromReusableSet(options);

        if (inBitmap != null) {
            options.inBitmap = inBitmap;
        }
    }

    private Bitmap getBitmapFromReusableSet(BitmapFactory.Options options) {
        Bitmap bitmap = null;

        if (mReusableBitmaps != null && !mReusableBitmaps.isEmpty()) {
            synchronized (mReusableBitmaps) {
                final Iterator<SoftReference<Bitmap>> iterator
                        = mReusableBitmaps.iterator();
                Bitmap item;

                while (iterator.hasNext()) {
                    item = iterator.next().get();

                    if (null != item && item.isMutable()) {
                        if (canUseForInBitmap(item, options)) {
                            bitmap = item;
                            iterator.remove();
                            break;
                        }
                    } else {
                        iterator.remove();
                    }
                }
            }
        }
        return bitmap;
    }

    private boolean canUseForInBitmap(Bitmap candidate, BitmapFactory.Options targetOptions) {
        int width = targetOptions.outWidth / targetOptions.inSampleSize;
        int height = targetOptions.outHeight / targetOptions.inSampleSize;
        int byteCount = width * height * getBytesPerPixel(candidate.getConfig());
        return byteCount <= candidate.getAllocationByteCount();
    }

    private int getBytesPerPixel(Bitmap.Config config) {
        switch (config) {
            case ARGB_8888:
                return 4;
            case RGB_565:
                return 2;
            case ARGB_4444:
                return 2;
            case ALPHA_8:
                return 1;
        }

        return 1;
    }
}
