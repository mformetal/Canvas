package milespeele.canvas.bitmap;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import milespeele.canvas.util.Logg;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;

/**
 * Created by mbpeele on 9/2/15.
 */
public class BitmapUtils {

    public static Observable<byte[]> compressBitmap(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        return Observable.just(stream.toByteArray());
    }

    public static void cacheBitmap(Context context, String filename, Bitmap bitmap) {
        try {
            final FileOutputStream fos = context.openFileOutput(filename, Context.MODE_PRIVATE);
            compressBitmap(bitmap)
                    .subscribeOn(AndroidSchedulers.mainThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(bytes -> {
                        try {
                            fos.write(bytes);
                            fos.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
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
        final int xStep = bitmap.getWidth() / 10;
        final int yStep = bitmap.getHeight() / 10;
        for (int x = 0; x < bitmap.getWidth(); x += xStep) {
            for (int y = 0; y < bitmap.getHeight(); y += yStep) {
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
                return pixel;
            }
        }

        return 0;
    }
}
