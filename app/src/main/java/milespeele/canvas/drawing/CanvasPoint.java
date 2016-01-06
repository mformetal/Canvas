package milespeele.canvas.drawing;

/**
 * Created by mbpeele on 11/29/15.
 */
class CanvasPoint {

    public float x, y;
    public long time;

    public CanvasPoint(float x, float y, long time) {
        this.x = x;
        this.y = y;
        this.time = time;
    }

    public float computeDistance(CanvasPoint p) {
        float dx = x - p.x;
        float dy = y - p.y;
        return (float) Math.sqrt(dx * dx + dy * dy);
    }

    public float computeVelocity(CanvasPoint p) {
        long duration = Math.abs(time - p.time);
        return (duration != 0) ? computeDistance(p) / duration : computeDistance(p);
    }

    public CanvasPoint computeMidpoint(CanvasPoint p2) {
        return new CanvasPoint((x + p2.x) / 2.0f, (y + p2.y) / 2.0f, (time + p2.time) / 2);
    }
}
