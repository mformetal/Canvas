package milespeele.canvas.asynctask;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

import java.io.ByteArrayOutputStream;
import java.lang.ref.WeakReference;

import javax.inject.Inject;

import milespeele.canvas.MainApp;
import milespeele.canvas.activity.ActivityHome;
import milespeele.canvas.parse.ParseUtils;

/**
 * Created by milespeele on 7/5/15.
 */
public class AsyncSave extends AsyncTask<Bitmap, Void, byte[]> {

    private int screenWidth;
    private int screenHeight;
    private WeakReference<ActivityHome> weakCxt;
    @Inject ParseUtils parseUtils;
    private String where;
    private String file;

    public AsyncSave(String where, String file, ActivityHome activity, int screenWidth, int screenHeight) {
        ((MainApp) activity.getApplication()).getApplicationComponent().inject(this);
        weakCxt = new WeakReference<>(activity);
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        this.where = where;
        this.file = file;
    }

    private int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }
        return inSampleSize;
    }

    @Override
    protected byte[] doInBackground(Bitmap... params) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = calculateInSampleSize(options, screenWidth, screenHeight);
        Bitmap scaledImage = Bitmap.createScaledBitmap(params[0], screenWidth, screenHeight, false);

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        scaledImage.compress(Bitmap.CompressFormat.PNG, 100, bos);
        return bos.toByteArray();
    }

    @Override
    protected void onPostExecute(byte[] result) {
        super.onPostExecute(result);
        ActivityHome activityHome = weakCxt.get();
        if (activityHome != null && !activityHome.isFinishing()) {
            activityHome.onByteArrayReceived(where, result, file);
        }
    }
}
