package milespeele.canvas.drawing;

/**
 * Created by mbpeele on 11/29/15.
 */
public class DrawingPoint {

    public float x, y;
    public long time;
    public float width;
    public int color;

    public DrawingPoint() {}

    public DrawingPoint(float x, float y, long time, float width, int color) {
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
        return new DrawingPoint((x + p2.x) / 2.0f, (y + p2.y) / 2, (time + p2.time) / 2, p2.width, p2.color);
    }
}
