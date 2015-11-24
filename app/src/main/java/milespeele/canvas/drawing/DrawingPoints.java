package milespeele.canvas.drawing;

import android.graphics.Paint;

import java.util.ArrayList;

/**
 * Created by mbpeele on 11/23/15.
 */
public class DrawingPoints extends ArrayList<DrawingPoint> {

    private float lastWidth, lastVelocity;
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
        lastWidth = 0;
        lastVelocity = 0;
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

    public void setRedrawPaint(Paint redrawPaint) {
        this.redrawPaint.set(redrawPaint);
    }
}
