package milespeele.canvas.drawing;

import android.graphics.Paint;

import java.util.ArrayList;

/**
 * Created by mbpeele on 11/23/15.
 */
public class DrawingPoints extends ArrayList<DrawingPoint> {

    public float lastWidth, lastVelocity;
    public Paint redrawPaint;

    public DrawingPoints(Paint paint) {
        super();
        redrawPaint = new Paint(paint);
    }

    public DrawingPoints(DrawingPoints other) {
        super(other);
        lastWidth = other.lastWidth;
        lastVelocity = other.lastVelocity;
        redrawPaint = new Paint(other.redrawPaint);
    }

    public DrawingPoint getLast() {
        return get(size() - 1);
    }

    @Override
    public void clear() {
        super.clear();
        lastWidth = 0;
        lastVelocity = 0;
    }
}
