package milespeele.canvas.drawing;

import android.net.Uri;

/**
 * Created by mbpeele on 1/4/16.
 */
public class DrawingBitmapPair {

    public Uri uri;
    public float[] matrixValues;

    public DrawingBitmapPair(Uri uri, float[] matrixValues) {
        this.uri = uri;
        this.matrixValues = matrixValues;
    }
}
