package milespeele.canvas.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import milespeele.canvas.util.Logg;
import rx.Observable;

/**
 * Created by Miles Peele on 9/23/2015.
 */
public class ServiceBitmapUtils extends IntentService {

    private final static String BITMAP_KEY = "bitmap";
    private final static String FILENAME = "name";

    public static Intent newIntent(Context context, byte[] array) {
        Intent intent = new Intent(context, ServiceBitmapUtils.class);
        intent.putExtra(BITMAP_KEY, array);
        return intent;
    }

    public ServiceBitmapUtils() {
        super("ServiceBitmapUtils");
    }

    public ServiceBitmapUtils(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            try {
                final FileOutputStream fos = openFileOutput(FILENAME, Context.MODE_PRIVATE);
                try {
                    fos.write(intent.getByteArrayExtra(BITMAP_KEY));
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
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
