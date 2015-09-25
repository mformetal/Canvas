package milespeele.canvas.paint;

import android.graphics.Paint;
import android.graphics.Path;

import java.util.ArrayList;

/**
 * Created by milespeele on 7/9/15.
 */
public class PaintPath extends Path {

    private Paint mPaint;
    private PathPoints mPoints;

    private static final float VELOCITY_FILTER_WEIGHT = 0.2f;
    private static final float TOLERANCE = 5f;
    private float lastVelocity;
    private float left, right, top, bottom = 0;

    public PaintPath(Paint paint) {
        mPaint = paint;
        mPoints = new PathPoints();
    }

    public Paint getPaint() { return mPaint; }

    public int getColor() { return mPaint.getColor(); }

    public void setColor(int color) {
        mPaint.setColor(color);
    }

    public float getStrokeWidth() { return mPaint.getStrokeWidth(); }

    @Override
    public void moveTo(float x, float y) {
        super.moveTo(x, y);
        int roundedX = Math.round(x);
        int roundedY = Math.round(y);
        left = roundedX;
        top = roundedY;
        right = roundedX;
        bottom = roundedY;
    }

    @Override
    public void lineTo(float x, float y) {
        super.lineTo(x, y);
        int roundedX = Math.round(x);
        int roundedY = Math.round(y);
        if (left < roundedX) {
            right = roundedX;
        } else {
            right = left;
            left = roundedX;
        }

        if (top < roundedY) {
            bottom = roundedY;
        } else {
            bottom = top;
            top = roundedY;
        }
    }

    public float getLastVelocity() {
        return (lastVelocity != 0) ? lastVelocity : 1;
    }

    public void addPoint(float x, float y, float time) {
        if (mPoints.size() > 0) {
            PathPoint prevPoint = mPoints.getLast();
            if (Math.abs(prevPoint.x - x) < TOLERANCE && Math.abs(prevPoint.y - y) < TOLERANCE) {
                return;
            }
        }

        mPoints.add(new PathPoint(x, y, time));

        PathPoint[] lastThree = mPoints.getLastThree();

        float velocity = lastThree[0].velocityFrom(lastThree[1]);
        velocity = VELOCITY_FILTER_WEIGHT * velocity + (1 - VELOCITY_FILTER_WEIGHT) * getLastVelocity();
        float strokeWidth = getStrokeWidth() - velocity;
    }

    public float getLeft() { return left; }
    public float getRight() { return right; }
    public float getBottom() { return bottom; }
    public float getTop() { return top; }

    public static class PathPoint {
        public final float x;
        public final float y;
        public final float time;

        public PathPoint(float x, float y, float time){
            this.x = x;
            this.y = y;
            this.time = time;
        }

        /**
         * Caculate the distance between current point to the other.
         * @param p the other point
         * @return
         */
        public float distanceTo(PathPoint p){
            return (float) (Math.sqrt(Math.pow((x - p.x), 2) + Math.pow((y - p.y), 2)));
        }


        /**
         * Caculate the velocity from the current point to the other.
         * @param p the other point
         * @return
         */
        public float velocityFrom(PathPoint p) {
            return distanceTo(p) / (this.time - p.time);
        }

        public PathPoint midPoint(PathPoint p2) {
            return new PathPoint((x + p2.x) / 2.0f, (y + p2.y) / 2, (time + p2.time) / 2);
        }
    }

    public static class PathPoints extends ArrayList<PathPoint> {

        public PathPoint getLast() {
            return get(size() - 1);
        }

        public PathPoint[] getLastThree() {
            return (canGetLastThree()) ?
                    new PathPoint[] {
                    get(size() - 1),
                    get(size() - 2),
                    get(size() - 3)} :
                    null;
        }

        public boolean canGetLastThree() {
            return size() >= 3;
        }
    }
}