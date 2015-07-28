package milespeele.canvas.paint;

import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;

import milespeele.canvas.util.Logg;

/**
 * Created by milespeele on 7/9/15.
 */
public class PaintPath extends Path {

    private Paint paint;
    private int color;

    public PaintPath(Paint paint) {
        this.paint = paint;
        this.color = paint.getColor();
    }

    public Paint getPaint() { return paint; }

    public int getColor() { return color; }

    public void setColor(int color) {
        this.color = color;
        paint.setColor(color);
    }

    private void addPoint(float x, float y) {

    }

    @Override
    public void moveTo(float x, float y) {
        super.moveTo(x, y);
    }

    private class Point {
        float x, y;

        Point(float x, float y) {
            this.x = x;
            this.y = y;
        }
    }
}