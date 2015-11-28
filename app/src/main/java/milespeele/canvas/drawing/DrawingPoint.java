package milespeele.canvas.drawing;

import android.graphics.Paint;

import com.google.common.base.Objects;

import milespeele.canvas.util.Logg;

/**
 * Created by mbpeele on 9/26/15.
 */
public class DrawingPoint {

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
