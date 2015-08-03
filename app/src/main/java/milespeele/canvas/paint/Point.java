package milespeele.canvas.paint;

public class Point {
    public final float x;
    public final float y;
    public final long time;

    public Point(float x, float y, long time){
        this.x = x;
        this.y = y;
        this.time = time;
    }

    /**
     * Caculate the distance between current point to the other.
     * @param p the other point
     * @return
     */
    public float distanceTo(Point p){
        return (float) (Math.sqrt(Math.pow((x - p.x), 2) + Math.pow((y - p.y), 2)));
    }


    /**
     * Caculate the velocity from the current point to the other.
     * @param p the other point
     * @return
     */
    public float velocityFrom(Point p) {
        return distanceTo(p) / (this.time - p.time);
    }

    public Point midPoint(Point p2) {
        return new Point((x + p2.x) / 2.0f, (y + p2.y) / 2, (time + p2.time) / 2);
    }
}