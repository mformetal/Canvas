package miles.scribble.ui.drawing;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.net.Uri;

import com.bumptech.glide.util.LruCache;
import com.google.common.io.ByteStreams;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import miles.scribble.util.FileUtils;

import static android.content.Context.ACTIVITY_SERVICE;
import static android.content.pm.ApplicationInfo.FLAG_LARGE_HEAP;

/**
 * Created by mbpeele on 1/5/16.
 */
class BitmapCache extends LruCache<Uri, Bitmap> {

    final Set<SoftReference<Bitmap>> mReusableBitmaps;
    final Context mContext;
    final int mViewWidth, mViewHeight;

    public BitmapCache(Context context) {
        super(getMaxSize(context));

        mReusableBitmaps = Collections.synchronizedSet(new HashSet<>());

        mContext = context;
        Point size = new Point();
        ((Activity) mContext).getWindowManager().getDefaultDisplay().getSize(size);
        mViewWidth = size.x;
        mViewHeight = size.y;
    }

    public static int getMaxSize(Context context) {
        ActivityManager am = (ActivityManager) context.getSystemService(ACTIVITY_SERVICE);
        boolean largeHeap = (context.getApplicationInfo().flags & FLAG_LARGE_HEAP) != 0;
        int memoryClass = am.getMemoryClass();
        if (largeHeap) {
            memoryClass = am.getLargeMemoryClass();
        }
        return 1024 * 1024 * memoryClass / 5;
    }

    @Override
    protected void onItemEvicted(Uri key, Bitmap item) {
        super.onItemEvicted(key, item);
        mReusableBitmaps.add(new SoftReference<>(item));
    }

    public Bitmap decodeStream(InputStream inputStream) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ByteStreams.copy(inputStream, out);
        ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());

        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        options.inMutable = true;
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;

        BitmapFactory.decodeStream(in, null, options);

        in.reset();

        options.inSampleSize = FileUtils.calculateInSampleSize(options, mViewWidth, mViewHeight);

        addInBitmapOptions(options);

        options.inJustDecodeBounds = false;

        return BitmapFactory.decodeStream(in, null, options);
    }

    private void addInBitmapOptions(BitmapFactory.Options options) {
        Bitmap inBitmap = getBitmapFromReusableSet(options);

        if (inBitmap != null) {
            options.inBitmap = inBitmap;
        }
    }

    private Bitmap getBitmapFromReusableSet(BitmapFactory.Options options) {
        Bitmap bitmap = null;

        if (mReusableBitmaps != null && !mReusableBitmaps.isEmpty()) {
            synchronized (mReusableBitmaps) {
                final Iterator<SoftReference<Bitmap>> iterator = mReusableBitmaps.iterator();
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

    private boolean canUseForInBitmap(
            Bitmap candidate, BitmapFactory.Options targetOptions) {
        int width = targetOptions.outWidth / targetOptions.inSampleSize;
        int height = targetOptions.outHeight / targetOptions.inSampleSize;
        int byteCount = width * height * getBytesPerPixel(candidate.getConfig());
        return byteCount <= candidate.getAllocationByteCount();
    }

    private int getBytesPerPixel(Bitmap.Config config) {
        if (config == Bitmap.Config.ARGB_8888) {
            return 4;
        } else if (config == Bitmap.Config.RGB_565) {
            return 2;
        } else if (config == Bitmap.Config.ARGB_4444) {
            return 2;
        } else if (config == Bitmap.Config.ALPHA_8) {
            return 1;
        }
        return 1;
    }
}
