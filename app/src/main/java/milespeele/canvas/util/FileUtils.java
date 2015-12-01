package milespeele.canvas.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Stack;

import milespeele.canvas.drawing.DrawingHistory;
import milespeele.canvas.drawing.DrawingPoints;
import rx.Observable;

/**
 * Created by Miles Peele on 9/23/2015.
 */
public class FileUtils {

    private final static String BITMAP_FILENAME = "canvas:bitmap";
    private final static String ALL_POINTS_FILENAME = "canvas:allPoints";
    private final static String REDO_POINTS_FILENAME = "canvas:redoPoints";

    public static void cacheBitmap(Context context, byte[] bytes) {
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
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        options.inMutable = true;
        options.inDither = true;
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

    public static void cacheAllHistory(Context context, DrawingHistory points) {
        try {
            ObjectOutputStream oos = new ObjectOutputStream(
                    context.openFileOutput(ALL_POINTS_FILENAME, Context.MODE_PRIVATE));
//            points.writeExternal(oos);
            oos.writeObject(points);
            oos.close();
        } catch (IOException e) {
            Logg.log(e);
            e.printStackTrace();
        }
    }

    public static void cacheRedoneHistory(Context context, DrawingHistory points) {
        try {
            ObjectOutputStream oos = new ObjectOutputStream(
                    context.openFileOutput(REDO_POINTS_FILENAME, Context.MODE_PRIVATE));
            oos.writeObject(points);
            oos.close();
        } catch (IOException e) {
            Logg.log(e);
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    public static DrawingHistory getAllHistory(Context context) {
        DrawingHistory points = new DrawingHistory();
        try {
            ObjectInputStream ois = new ObjectInputStream(context.openFileInput(ALL_POINTS_FILENAME));
            points = (DrawingHistory) ois.readObject();
            ois.close();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return points;
    }

    @SuppressWarnings("unchecked")
    public static DrawingHistory getRedoneHistory(Context context) {
        DrawingHistory points = new DrawingHistory();
        try {
            ObjectInputStream ois = new ObjectInputStream(context.openFileInput(REDO_POINTS_FILENAME));
            points = (DrawingHistory) ois.readObject();
            ois.close();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return points;
    }

    public static void deleteAllHistoryFile(Context context) {
        context.deleteFile(ALL_POINTS_FILENAME);
    }

    public static void deleteRedoneHistoryFile(Context context) {
        context.deleteFile(REDO_POINTS_FILENAME);
    }

    public static void deleteBitmapFile(Context context) {
        context.deleteFile(BITMAP_FILENAME);
    }
}
