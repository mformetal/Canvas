package milespeele.canvas.drawing;

import android.graphics.Paint;

import java.util.ArrayList;

/**
 * Created by mbpeele on 11/23/15.
 */
public class DrawingPoints extends ArrayList<DrawingPoint> {

    private float lastWidth = 1, lastVelocity = 1;
    private Paint redrawPaint;

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
        redrawPaint.reset();
        lastWidth = 1;
        lastVelocity = 1;
    }

    public float getLastWidth() {
        return lastWidth;
    }

    public void setLastWidth(float lastWidth) {
        this.lastWidth = lastWidth;
    }

    public float getLastVelocity() {
        return lastVelocity;
    }

    public void setLastVelocity(float lastVelocity) {
        this.lastVelocity = lastVelocity;
    }

    public Paint getRedrawPaint() {
        return redrawPaint;
    }

    public void setRedrawPaint(Paint paint) {
        redrawPaint.set(paint);
    }
}
