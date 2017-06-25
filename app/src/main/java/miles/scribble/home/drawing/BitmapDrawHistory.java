package miles.scribble.home.drawing;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.net.Uri;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by mbpeele on 1/4/16.
 */
class BitmapDrawHistory {

    public Uri uri;
    public float[] matrixValues;

    public BitmapDrawHistory(Uri uri, float[] matrixValues) {
        this.uri = uri;
        this.matrixValues = matrixValues;
    }

    public void draw(Matrix matrix, BitmapCache cache, Context context, Canvas canvas) {
        float[] prevMatrixValues = new float[9];
        matrix.getValues(prevMatrixValues);

        Bitmap bitmap = cache.get(uri);
        if (bitmap == null) {
            InputStream inputStream;
            try {
                inputStream = context.getContentResolver().openInputStream(uri);
                bitmap = cache.decodeStream(inputStream);

                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        matrix.setValues(matrixValues);

        canvas.save();
        canvas.concat(matrix);
        canvas.drawBitmap(bitmap, 0, 0, null);
        canvas.restore();

        matrix.setValues(prevMatrixValues);
    }
}
