package milespeele.canvas.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Scanner;

import rx.Observable;

/**
 * Created by Miles Peele on 9/23/2015.
 */
public class FileUtils {

    private final static String BITMAP_FILENAME = "canvas:bitmap";
    private final static String COLORS_FILENAME = "canvas:colors";

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

    public static void cacheColors(Context context, ArrayList<Integer> colors) {
        try {
            FileOutputStream fos = context.openFileOutput(COLORS_FILENAME, Context.MODE_PRIVATE);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(colors);
            oos.close();
        } catch(IOException e){
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    public static ArrayList<Integer> getColors(Context context) {
        ArrayList<Integer> colors = new ArrayList<>();
        try{
            FileInputStream fis = context.openFileInput(COLORS_FILENAME);
            ObjectInputStream ois = new ObjectInputStream(fis);
            colors = (ArrayList<Integer>) ois.readObject();
        } catch (ClassNotFoundException | IOException e) {
            e.printStackTrace();
        }
        return colors;
    }
}
