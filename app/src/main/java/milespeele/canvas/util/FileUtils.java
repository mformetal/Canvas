package milespeele.canvas.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Paint;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.google.common.primitives.Ints;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Stack;

import milespeele.canvas.drawing.DrawingHistory;
import milespeele.canvas.drawing.DrawingPoint;
import milespeele.canvas.drawing.DrawingPoints;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by Miles Peele on 9/23/2015.
 */
public class FileUtils {

    public final static String BITMAP_FILENAME = "canvas:bitmap";

    private Context context;

    public FileUtils(Context otherContext) {
        context = otherContext;
    }

    public static void compressAndCache(Bitmap bitmap, Context context) {
        compressBitmapAsObservable(bitmap)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(bytes -> {
                    try {
                        Output output = new Output(context.openFileOutput(BITMAP_FILENAME, Context.MODE_PRIVATE));
                        output.write(bytes);
                        output.flush();
                        output.close();
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                });
    }

    public static Observable<byte[]> compressBitmapAsObservable(Bitmap bitmap) {
        return Observable.just(compressBitmapAsByteArray(bitmap));
    }

    public static byte[] compress(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        return stream.toByteArray();
    }

    public static byte[] compressBitmapAsByteArray(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        return stream.toByteArray();
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
            Input test = new Input(context.openFileInput(BITMAP_FILENAME));
            bitmap = BitmapFactory.decodeStream(test, null, getBitmapOptions(context));
            test.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return bitmap;
    }

    public static void deleteBitmapFile(Context context) {
        context.deleteFile(BITMAP_FILENAME);
    }
}
