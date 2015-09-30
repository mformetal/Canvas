package milespeele.canvas.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import rx.Observable;
import rx.schedulers.Schedulers;

/**
 * Created by Miles Peele on 9/23/2015.
 */
public class BitmapUtils {

    private final static String FILENAME = "name";

    public static void cacheBitmap(Context context, Bitmap bitmap) {
        compressBitmapAsObservable(bitmap)
                .subscribeOn(Schedulers.io())
                .subscribe(bytes -> {
                    try {
                        final FileOutputStream fos = context.openFileOutput(FILENAME, Context.MODE_PRIVATE);
                        try {
                            fos.write(compressBitmapAsByteArray(bitmap));
                            fos.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                });
    }

    public static Observable<byte[]> compressBitmapAsObservable(Bitmap bitmap) {
        return Observable.just(compressBitmapAsByteArray(bitmap));
    }

    public static byte[] compressBitmapAsByteArray(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        return stream.toByteArray();
    }

    public static Bitmap getCachedBitmap(Context context) {
        Bitmap bitmap = null;

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        options.inMutable = true;
        options.inDither = true;
        options.inPreferQualityOverSpeed = true;

        try {
            FileInputStream test = context.openFileInput(FILENAME);
            bitmap = BitmapFactory.decodeStream(test, null, options);
            test.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return bitmap;
    }
}
