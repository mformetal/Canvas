package milespeele.canvas.drawing;

import android.graphics.Paint;

import milespeele.canvas.util.Logg;

/**
 * Created by mbpeele on 9/26/15.
 */
public class DrawingPoint {

    public float x;
    public float y;
    public float width;
    public long time;

    public DrawingPoint(float x, float y, long time) {
        this.x = x;
        this.y = y;
        this.time = time;
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

    public float getPercX(DrawingPoint p, float percentage) {
        return x + ((p.x - x) * percentage);
    }

    public float getPercY(DrawingPoint p, float percentage) {
        return y + ((p.y - y) * percentage);
    }

    @Override
    public String toString() {
        return "X: " + x + ", " + "Y: " + y + "TIME: " + time;
    }
}
