package miles.scribble.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
/**
 * Created by Miles Peele on 9/23/2015.
 */
public class FileUtils {

    public final static String DRAWING_BITMAP_FILENAME = "canvas:bitmap";

    public static BitmapFactory.Options getBitmapOptions() {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        options.inMutable = true;
        options.inPreferQualityOverSpeed = true;
        return options;
    }

    public static int calculateInSampleSize(BitmapFactory.Options options,
                                            int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    @Nullable
    public static Bitmap getCachedBitmap(Context context) {
        Bitmap bitmap = null;
        try {
            InputStream test = context.openFileInput(DRAWING_BITMAP_FILENAME);
            bitmap = BitmapFactory.decodeStream(test, null, getBitmapOptions());
            test.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return bitmap;
    }
}
