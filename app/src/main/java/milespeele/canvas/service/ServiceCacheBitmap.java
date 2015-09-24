package milespeele.canvas.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import milespeele.canvas.util.BitmapUtils;
import milespeele.canvas.util.Logg;

/**
 * Created by Miles Peele on 9/23/2015.
 */
public class ServiceCacheBitmap extends IntentService {

    private final static String BITMAP_KEY = "bitmap";
    private final static String FILENAME = "name";

    public static Intent newIntent(Context context, byte[] array, String filename) {
        Intent intent = new Intent(context, ServiceCacheBitmap.class);
        intent.putExtra(BITMAP_KEY, array);
        intent.putExtra(FILENAME, filename);
        return intent;
    }

    public ServiceCacheBitmap() {
        super("ServiceCacheBitmap");
    }

    public ServiceCacheBitmap(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            try {
                final FileOutputStream fos = openFileOutput(intent.getStringExtra(FILENAME), Context.MODE_PRIVATE);
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
}
