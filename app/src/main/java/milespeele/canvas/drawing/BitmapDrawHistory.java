package milespeele.canvas.drawing;

import android.net.Uri;

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
}
