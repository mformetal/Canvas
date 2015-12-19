package milespeele.canvas.drawing;

import android.graphics.Paint;

import java.util.ArrayList;

/**
 * Created by mbpeele on 11/23/15.
 */
public class DrawingPoints extends ArrayList<DrawingPoint> {

    public float lastWidth = 1, lastVelocity = 1;
    public float[] redrawPts = new float[0];

    public Paint redrawPaint;

    public DrawingPoints() {
        redrawPaint = new Paint();
    }

    public DrawingPoints(Paint paint) {
        super();
        redrawPaint = new Paint(paint);
    }

    public DrawingPoints(DrawingPoints other) {
        super(other);
        redrawPts = new float[other.redrawPts.length];
        System.arraycopy(other.redrawPts, 0, redrawPts, 0, other.redrawPts.length);

        lastWidth = other.lastWidth;
        lastVelocity = other.lastVelocity;
        redrawPaint = new Paint(other.redrawPaint);
    }

    public DrawingPoint peek() {
        return get(size() - 1);
    }

    @Override
    public void clear() {
        super.clear();
        lastWidth = 1;
        lastVelocity = 1;
    }

    public void storePoints() {
        int n = size() * 2;
        int arraySize = n + (n - 4);

        if (arraySize <= 0) {
            return;
        }

        redrawPts = new float[arraySize];
        int counter = 1;

        for (int ndx = 0; ndx < size(); ndx++) {
            float x = get(ndx).x, y = get(ndx).y;

            if (ndx == 0) {
                redrawPts[ndx] = x;
                redrawPts[ndx + 1] = y;
                continue;
            }

            if (ndx == size() - 1) {
                redrawPts[redrawPts.length - 2] = get(ndx).x;
                redrawPts[redrawPts.length - 1] = get(ndx).y;
                break;
            }

            int newNdx = ndx + (counter * 1);
            counter += 3;

            redrawPts[newNdx] = x;
            redrawPts[newNdx + 1] = y;
            redrawPts[newNdx + 2] = x;
            redrawPts[newNdx + 3] = y;
        }
    }
}
