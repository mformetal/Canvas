package milespeele.canvas.drawing;

import android.graphics.Paint;

import java.util.ArrayList;

/**
 * Created by mbpeele on 11/23/15.
 */
public class DrawingPoints extends ArrayList<DrawingPoints.DrawingPoint> {

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

    public static class DrawingPoint {

        public float x;
        public float y;
        public long time;
        public float width;
        public int color;

        public DrawingPoint(float x, float y, long time) {
            init(x, y, time, 0, 0);
        }

        public DrawingPoint(float x, float y, long time, float width, int color) {
            init(x, y, time, width, color);
        }

        private void init(float x, float y, long time, float width, int color) {
            this.x = x;
            this.y = y;
            this.time = time;
            this.width = width;
            this.color = color;
        }

        public float distanceTo(DrawingPoint p) {
            float dx = x - p.x;
            float dy = y - p.y;
            return (float) Math.sqrt(dx * dx + dy * dy);
        }

        public float velocityFrom(DrawingPoint p) {
            long duration = Math.abs(time - p.time);
            return (duration != 0) ? distanceTo(p) / duration : distanceTo(p);
        }

        public DrawingPoint midPoint(DrawingPoint p2) {
            return new DrawingPoint((x + p2.x) / 2.0f, (y + p2.y) / 2, (time + p2.time) / 2);
        }
    }

}
