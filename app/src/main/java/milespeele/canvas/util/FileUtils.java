package milespeele.canvas.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by Miles Peele on 9/23/2015.
 */
public class FileUtils {

    public final static String DRAWING_BITMAP_FILENAME = "canvas:bitmap";
    public final static String PHOTO_BITMAP_FILENAME = "canvasphoto";

    public static void cacheInBackground(Bitmap bitmap, Context context) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] bytes =  stream.toByteArray();

        Output output = null;
        try {
            output = new Output(context.openFileOutput(DRAWING_BITMAP_FILENAME, Context.MODE_PRIVATE));
            output.write(bytes);
        } catch (FileNotFoundException exception) {
            Logg.log(exception);
        } finally {
            if (output != null) {
                output.flush();
                output.close();
            }
        }
    }

    public static Observable<byte[]> cacheAsObservable(Bitmap bitmap, Context context) {
        return Observable.create(new Observable.OnSubscribe<byte[]>() {
            @Override
            public void call(Subscriber<? super byte[]> subscriber) {
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                byte[] bytes =  stream.toByteArray();

                subscriber.onNext(bytes);
            }
        })
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread());
    }

    public static BitmapFactory.Options getBitmapOptions(Context context) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        options.inMutable = true;
        options.inPreferQualityOverSpeed = true;
        options.inScaled = false;
        options.inJustDecodeBounds = false;

        DisplayMetrics metrics = new DisplayMetrics();
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        windowManager.getDefaultDisplay().getMetrics(metrics);
        options.inDensity = metrics.densityDpi;
        return options;
    }

    public static Bitmap getCachedBitmap(Context context) {
        Bitmap bitmap = null;

        try {
            Input test = new Input(context.openFileInput(DRAWING_BITMAP_FILENAME));
            bitmap = BitmapFactory.decodeStream(test, null, getBitmapOptions(context));
            test.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return bitmap;
    }

    public static void deleteBitmapFile(Context context, String name) {
        context.deleteFile(name);
    }

    public static File createPhotoFile(Context context) throws IOException {
        return File.createTempFile(PHOTO_BITMAP_FILENAME, ".jpg",
                context.getExternalFilesDir(Environment.DIRECTORY_PICTURES));
    }
}
