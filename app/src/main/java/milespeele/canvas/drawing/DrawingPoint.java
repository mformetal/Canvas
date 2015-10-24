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
    public int undo;

    public DrawingPoint(float x, float y, long time) {
        this.x = x;
        this.y = y;
        this.time = time;
    }

    public DrawingPoint(float x, float y, long time, float width) {
        this.x = x;
        this.y = y;
        this.time = time;
        this.width = width;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DrawingPoint)) return false;
        DrawingPoint point = (DrawingPoint) o;
        return Objects.equal(Math.round(x), Math.round(point.x)) &&
                Objects.equal(Math.round(y), Math.round(point.y));
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(x, y, time, width);
    }

    @Override
    public String toString() {
        return "X: " + x + ", " + "Y: " + y + ", " + "TIME: " + time;
    }
}
