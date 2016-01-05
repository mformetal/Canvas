package milespeele.canvas.drawing;

import android.graphics.Paint;

import java.util.ArrayList;

/**
 * Created by mbpeele on 1/4/16.
 */
class NormalDrawHistory {

    public float[] lines;
    public Paint paint;

    public NormalDrawHistory(ArrayList<CanvasPoint> points, Paint paint) {
        this.paint = new Paint(paint);
        lines = storePoints(points);
    }

    private float[] storePoints(ArrayList<CanvasPoint> points) {
        int length = points.size();

        int n = length * 2;
        int arraySize = n + (n - 4);

        if (arraySize <= 0) {
            throw new UnsupportedOperationException("Drawing Points length is 0");
        }

        float[] pts = new float[arraySize];
        int counter = 1;

        for (int ndx = 0; ndx < length; ndx++) {
            float x = points.get(ndx).x, y = points.get(ndx).y;

            if (ndx == 0) {
                pts[ndx] = x;
                pts[ndx + 1] = y;
                continue;
            }

            if (ndx == length - 1) {
                pts[pts.length - 2] = points.get(ndx).x;
                pts[pts.length - 1] = points.get(ndx).y;
                break;
            }

            int newNdx = ndx + (counter);
            counter += 3;

            pts[newNdx] = x;
            pts[newNdx + 1] = y;
            pts[newNdx + 2] = x;
            pts[newNdx + 3] = y;
        }
        return pts;
    }

}
