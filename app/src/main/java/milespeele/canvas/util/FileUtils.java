package milespeele.canvas.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.net.Uri;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func0;
import rx.schedulers.Schedulers;

/**
 * Created by Miles Peele on 9/23/2015.
 */
public class FileUtils {

    public final static String DRAWING_BITMAP_FILENAME = "canvas:bitmap";
    private static ArrayList<String> mFilenames;

    public static Observable<byte[]> cacheAsObservable(Bitmap bitmap, Context context) {
        return Observable.create(new Observable.OnSubscribe<byte[]>() {
            @Override
            public void call(Subscriber<? super byte[]> subscriber) {
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                byte[] bytes = stream.toByteArray();

                FileOutputStream output = null;
                try {
                    output = context.openFileOutput(DRAWING_BITMAP_FILENAME, Context.MODE_PRIVATE);
                    output.write(bytes);

                    subscriber.onNext(bytes);
                } catch (IOException e) {
                    Logg.log(e);
                } finally {
                    if (output != null) {
                        try {
                            output.flush();
                            output.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }

                subscriber.onCompleted();
            }
        });
    }

    public static BitmapFactory.Options getBitmapOptions() {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        options.inMutable = true;
        return options;
    }

    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    public static Bitmap getCachedBitmap(Context context) {
        Point size = new Point();
        ((Activity) context).getWindowManager().getDefaultDisplay().getSize(size);
        int w = size.x;
        int h = size.y;

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

    public static Bitmap getBitmapFromStream(InputStream inputStream) {
        return BitmapFactory.decodeStream(inputStream, null, getBitmapOptions());
    }

    public static void deleteBitmapFile(Context context, String name) {
        context.deleteFile(name);
    }

    public static File createPhotoFile() throws IOException {
        if (mFilenames == null) {
            mFilenames = new ArrayList<>();
        }

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        mFilenames.add(imageFileName + ".jpg");
        return File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
    }

    public static Uri addFileToGallery(Context context, String filePath) {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(filePath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        context.sendBroadcast(mediaScanIntent);
        return contentUri;
    }

    public static void deleteTemporaryFiles(Context context) {
        if (mFilenames != null) {
            for (String name: mFilenames) {
                context.deleteFile(name);
            }
        }
    }
}
