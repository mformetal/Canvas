package milespeele.canvas.asynctask;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

import java.io.ByteArrayOutputStream;
import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;

import javax.inject.Inject;

import milespeele.canvas.MainApp;
import milespeele.canvas.activity.ActivityHome;
import milespeele.canvas.parse.ParseUtils;
import milespeele.canvas.util.Logger;

/**
 * Created by milespeele on 7/5/15.
 */
public class AsyncBitmap extends AsyncTask<Bitmap, Void, byte[]> {

    private int screenWidth;
    private int screenHeight;
    private WeakReference<ActivityHome> weakCxt;
    @Inject ParseUtils parseUtils;
    private String file;

    public AsyncBitmap(String file, ActivityHome activity, int screenWidth, int screenHeight) {
        ((MainApp) activity.getApplication()).getApplicationComponent().inject(this);
        weakCxt = new WeakReference<>(activity);
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        this.file = file;
    }

    @Override
    protected byte[] doInBackground(Bitmap... params) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        params[0].compress(Bitmap.CompressFormat.JPEG, 100, stream);
        return stream.toByteArray();
    }

    @Override
    protected void onPostExecute(byte[] result) {
        super.onPostExecute(result);
        ActivityHome activityHome = weakCxt.get();
        if (activityHome != null && !activityHome.isFinishing()) {
            activityHome.onByteArrayReceived(result, file);
        }
    }
}
