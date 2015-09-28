package milespeele.canvas.drawing;

import milespeele.canvas.util.Logg;

/**
 * Created by mbpeele on 9/26/15.
 */
public class DrawingPoint {

    public final float x;
    public final float y;
    public final float time;

    public DrawingPoint(float x, float y, float time){
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
}
