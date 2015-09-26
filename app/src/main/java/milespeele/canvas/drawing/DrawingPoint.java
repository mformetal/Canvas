package milespeele.canvas.drawing;

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
        return (float) (Math.sqrt(Math.pow((x - p.x), 2) + Math.pow((y - p.y), 2)));
    }

    public float velocityFrom(DrawingPoint p) {
        return distanceTo(p) / (this.time - p.time);
    }

    public DrawingPoint midPoint(DrawingPoint p2) {
        return new DrawingPoint((x + p2.x) / 2.0f, (y + p2.y) / 2, (time + p2.time) / 2);
    }
}
