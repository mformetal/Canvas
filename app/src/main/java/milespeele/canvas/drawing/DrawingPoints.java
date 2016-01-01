package milespeele.canvas.drawing;

import android.graphics.Paint;

import java.util.ArrayList;

/**
 * Created by mbpeele on 11/23/15.
 */
public class DrawingPoints extends ArrayList<DrawingPoint> {

    public float[] redrawPts = new float[0];

    public Paint redrawPaint;

    public DrawingPoints(Paint paint) {
        super();
        redrawPaint = new Paint(paint);
    }

    public DrawingPoints(DrawingPoints other) {
        super(other);
        storePoints(other.size(), other);
        redrawPaint = new Paint(other.redrawPaint);
    }

    public DrawingPoint peek() {
        return get(size() - 1);
    }

    public void storePoints(int length, DrawingPoints other) {
        int n = length * 2;
        int arraySize = n + (n - 4);

        if (arraySize <= 0) {
            return;
        }

        redrawPts = new float[arraySize];
        int counter = 1;

        for (int ndx = 0; ndx < length; ndx++) {
            float x = other.get(ndx).x, y = other.get(ndx).y;

            if (ndx == 0) {
                redrawPts[ndx] = x;
                redrawPts[ndx + 1] = y;
                continue;
            }

            if (ndx == size() - 1) {
                redrawPts[redrawPts.length - 2] = other.get(ndx).x;
                redrawPts[redrawPts.length - 1] = other.get(ndx).y;
                break;
            }

            int newNdx = ndx + (counter);
            counter += 3;

            redrawPts[newNdx] = x;
            redrawPts[newNdx + 1] = y;
            redrawPts[newNdx + 2] = x;
            redrawPts[newNdx + 3] = y;
        }
    }
}
