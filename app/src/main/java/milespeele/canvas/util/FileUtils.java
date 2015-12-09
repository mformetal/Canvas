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
import rx.schedulers.Schedulers;

/**
 * Created by Miles Peele on 9/23/2015.
 */
public class FileUtils {

    private final static String BITMAP_FILENAME = "canvas:bitmap";
    private final static String COLORS_FILENAME = "canvas:colors";

    private Context context;

    public FileUtils(Context otherContext) {
        context = otherContext;
    }

    public void cacheBitmap(Bitmap bitmap) {
        compressBitmapAsObservable(bitmap)
                .subscribeOn(Schedulers.io())
                .subscribe(bytes -> {
                    try {
                        Output output = new Output(context.openFileOutput(BITMAP_FILENAME, Context.MODE_PRIVATE));
                        output.write(bytes);
                        output.close();
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                });
    }

    public Observable<byte[]> compressBitmapAsObservable(Bitmap bitmap) {
        return Observable.just(compressBitmapAsByteArray(bitmap));
    }

    public static Observable<byte[]> compress(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        return Observable.just(stream.toByteArray());
    }

    public byte[] compressBitmapAsByteArray(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        return stream.toByteArray();
    }

    public Bitmap getCachedBitmap() {
        Bitmap bitmap = null;

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

        try {
            Input test = new Input(context.openFileInput(BITMAP_FILENAME));
            bitmap = BitmapFactory.decodeStream(test, null, options);
            test.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return bitmap;
    }

    public void cacheColors(ArrayList<Integer> colors) {
        try {
            Output output = new Output(context.openFileOutput(COLORS_FILENAME, Context.MODE_PRIVATE));
            output.writeInt(colors.size());
            output.writeInts(Ints.toArray(colors));
            output.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public ArrayList<Integer> getColors() {
        ArrayList<Integer> colors = new ArrayList<>();
        try {
            Input input = new Input(context.openFileInput(COLORS_FILENAME));
            int length = input.readInt();
            int[] ints = input.readInts(length);
            for (int primitive: ints) { colors.add(primitive); }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return colors;
    }

    public void deleteBitmapFile() {
        context.deleteFile(BITMAP_FILENAME);
    }
}
