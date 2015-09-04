package milespeele.canvas.util;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by mbpeele on 9/2/15.
 */
public class BitmapUtils {

    public static void cacheBitmap(Context context, Bitmap bitmap, String filename) {
        try {
            final FileOutputStream fos = context.openFileOutput(filename, Context.MODE_PRIVATE);
            try {
                fos.write(compressBitmapAsBitmapArray(bitmap));
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static Observable<byte[]> compressBitmapAsObservable(Bitmap bitmap) {
        return Observable.just(compressBitmapAsBitmapArray(bitmap));
    }

    public static byte[] compressBitmapAsBitmapArray(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        return stream.toByteArray();
    }

    public static Bitmap getCachedBitmap(Context context, String filename) {
        Bitmap bitmap = null;

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        options.inMutable = true;
        try {
            FileInputStream test = context.openFileInput(filename);
            bitmap = BitmapFactory.decodeStream(test, null, options);
            test.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return (bitmap != null) ? bitmap : null;
    }

    public static int getBitmapBackgroundColor(Bitmap bitmap) {
        Map<Integer, Integer> frequencies = new HashMap<>();
        final int xInc = bitmap.getWidth() / 20;
        final int yInc = bitmap.getHeight() / 20;
        for (int x = 0; x < bitmap.getWidth(); x += xInc) {
            for (int y = 0; y < bitmap.getHeight(); y += yInc) {
                int pixel = bitmap.getPixel(x, y);
                if (frequencies.containsKey(pixel)) {
                    frequencies.put(pixel, frequencies.get(pixel) + 1);
                } else {
                    frequencies.put(pixel, 0);
                }
            }
        }

        int maxFreq = Collections.max(frequencies.values());
        for (int pixel: frequencies.keySet()) {
            if (frequencies.get(pixel) == maxFreq) {
                return (pixel == 0) ? Color.WHITE : pixel;
            }
        }

        return Color.WHITE;
    }
}
