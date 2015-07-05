package milespeele.canvas.asynctask;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

import java.io.ByteArrayOutputStream;
import java.lang.ref.WeakReference;

import javax.inject.Inject;

import milespeele.canvas.MainApp;
import milespeele.canvas.activity.ActivityHome;
import milespeele.canvas.util.ParseUtils;

/**
 * Created by milespeele on 7/5/15.
 */
public class AsyncSave extends AsyncTask<Bitmap, Void, byte[]> {

    private int screenWidth;
    private int screenHeight;
    private WeakReference<ActivityHome> weakCxt;
    @Inject ParseUtils parseUtils;

    public AsyncSave(ActivityHome activity, int screenWidth, int screenHeight) {
        ((MainApp) activity.getApplication()).getApplicationComponent().inject(this);
        weakCxt = new WeakReference<ActivityHome>(activity);
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
    }

    private int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            // Calculate ratios of height and width to requested height and width
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);

            // Choose the smallest ratio as inSampleSize value, this will guarantee
            // a final image with both dimensions larger than or equal to the
            // requested height and width.
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
        ActivityHome activity = weakCxt.get();
        if (activity != null) {
            parseUtils.saveImage(activity, result);
        }
    }
}
