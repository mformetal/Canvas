package milespeele.canvas.drawing;

import android.graphics.Paint;

/**
 * Created by mbpeele on 9/26/15.
 */
public class DrawingPoint {

    public float x;
    public float y;
    public float time;

    public float fromX;
    public float fromY;
    public float toX;
    public float toY;
    public float width;
    public int color;
    public Paint paint;

    public DrawingPoint(float x, float y, float time) {
        this.x = x;
        this.y = y;
        this.time = time;
    }

    public DrawingPoint(float fromX, float fromY, float toX, float toY, float width, int color, Paint paint) {
        this.fromX = fromX;
        this.fromY = fromY;
        this.toX = toX;
        this.toY = toY;
        this.width = width;
        this.color = color;
        this.paint = paint;
    }

    public float distanceTo(DrawingPoint p) {
        float dx = x - p.x;
        float dy = y - p.y;
        return (float) Math.sqrt(dx * dx + dy * dy);
    }

    public float velocityFrom(DrawingPoint p) {
        return distanceTo(p) / Math.abs(time - p.time);
    }

    public DrawingPoint midPoint(DrawingPoint p2) {
        return new DrawingPoint((x + p2.x) / 2.0f, (y + p2.y) / 2, (time + p2.time) / 2);
    }

    public float getMidX(DrawingPoint p, float percentage) {
        return x + ((p.x - x) * percentage);
    }

    public float getMidY(DrawingPoint p, float percentage) {
        return y + ((p.y - y) * percentage);
    }

    @Override
    public String toString() {
        return "X: " + x + ", " + "Y: " + y + "TIME: " + time;
    }
}
