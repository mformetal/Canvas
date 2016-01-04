package milespeele.canvas.drawing;

import android.net.Uri;

/**
 * Created by mbpeele on 1/4/16.
 */
public class DrawingBitmapPair {

    public String string;
    public Uri uri;
    public float[] matrixValues;

    public DrawingBitmapPair(String string, float[] matrixValues) {
        this.string = string;
        this.matrixValues = matrixValues;
    }

    public DrawingBitmapPair(Uri uri, float[] matrixValues) {
        this.uri = uri;
        this.matrixValues = matrixValues;
    }
}
