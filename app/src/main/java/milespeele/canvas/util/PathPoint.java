package milespeele.canvas.util;

public class PathPoint {

    public final float x;
    public final float y;
    public final long time;

    public PathPoint(float x, float y, long time){
        this.x = x;
        this.y = y;
        this.time = time;
    }

    public float distanceTo(PathPoint p){
        return (float) (Math.sqrt(Math.pow((x - p.x), 2) + Math.pow((y - p.y), 2)));
    }

    public float velocityFrom(PathPoint p) {
        return distanceTo(p) / (this.time - p.time);
    }
}
