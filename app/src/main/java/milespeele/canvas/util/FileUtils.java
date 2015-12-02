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

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
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
    private final static String ALL_POINTS_FILENAME = "canvas:allPoints";
    private final static String REDO_POINTS_FILENAME = "canvas:redoPoints";

    private Context context;
    private Kryo kryo;

    public FileUtils(Context otherContext) {
        context = otherContext;

        kryo = new Kryo();
        kryo.register(DrawingHistory.class, 0);
        kryo.register(DrawingPoints.class, 1);
        kryo.register(DrawingPoint.class, 2);
        kryo.register(SerializablePaint.class, 3);
    }

    public void cacheBitmap(Bitmap bitmap) {
        compressBitmapAsObservable(bitmap)
                .subscribeOn(Schedulers.io())
                .subscribe(bytes -> {
                    try {
                        final FileOutputStream fos = context.openFileOutput(BITMAP_FILENAME, Context.MODE_PRIVATE);
                        try {
                            fos.write(bytes);
                            fos.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
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
            FileInputStream test = context.openFileInput(BITMAP_FILENAME);
            bitmap = BitmapFactory.decodeStream(test, null, options);
            test.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return bitmap;
    }

    public void cacheAllHistory(DrawingHistory points) {
        try {
            Output output = new Output(context.openFileOutput(ALL_POINTS_FILENAME, Context.MODE_PRIVATE));
            kryo.writeObject(output, points);
            output.close();
        } catch (FileNotFoundException e) {
//            Logg.log(e);
            e.printStackTrace();
        }
    }

    public void cacheRedoneHistory(DrawingHistory points) {
        try {
            Output output = new Output(context.openFileOutput(REDO_POINTS_FILENAME, Context.MODE_PRIVATE));
            kryo.writeObject(output, points);
            output.close();
        } catch (FileNotFoundException e) {
//            Logg.log(e);
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    public DrawingHistory getAllHistory() {
        DrawingHistory points = new DrawingHistory();
        try {
            Input input = new Input(context.openFileInput(ALL_POINTS_FILENAME));
            points = kryo.readObject(input, DrawingHistory.class);
            input.close();
        } catch (IOException e) {
//            Logg.log(e);
            e.printStackTrace();
        }
        return points;
    }

    @SuppressWarnings("unchecked")
    public DrawingHistory getRedoneHistory() {
        DrawingHistory points = new DrawingHistory();
        try {
            Input input = new Input(context.openFileInput(REDO_POINTS_FILENAME));
            points = kryo.readObject(input, DrawingHistory.class);
            input.close();
        } catch (IOException e) {
//            Logg.log(e);
            e.printStackTrace();
        }
        return points;
    }

    public void deleteAllHistoryFile() {
        context.deleteFile(ALL_POINTS_FILENAME);
    }

    public void deleteRedoneHistoryFile() {
        context.deleteFile(REDO_POINTS_FILENAME);
    }

    public void deleteBitmapFile() {
        context.deleteFile(BITMAP_FILENAME);
    }
}
