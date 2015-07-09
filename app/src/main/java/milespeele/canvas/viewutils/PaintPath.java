package milespeele.canvas.viewutils;

import android.graphics.Path;

/**
 * Created by milespeele on 7/9/15.
 */
public class PaintPath extends Path {


    private int color;

    public PaintPath(int color) {
        this.color = color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public int getColor() {
        return color;
    }

    public class Point {
        public float x, y, time;

        public Point(float x, float y, float time) {
            this.x = x;
            this.y = y;
            this.time = time;
        }

        public float velocityFrom(Point from) {
            return distanceTo(from) / (this.time - from.time);
        }

        public float distanceTo(Point toPoint) {
            float dx = Math.abs(x - toPoint.x);
            float dy = Math.abs(y - toPoint.y);
            return (float) Math.sqrt((dx * dx) + (dy * dy));
        }
    }

}
