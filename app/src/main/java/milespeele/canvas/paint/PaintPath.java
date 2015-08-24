package milespeele.canvas.paint;

import android.graphics.Paint;
import android.graphics.Path;

import rx.Observable;

/**
 * Created by milespeele on 7/9/15.
 */
public class PaintPath extends Path {

    private Paint paint;
    private float left = 0;
    private float right = 0;
    private float top = 0;
    private float bottom = 0;

    public PaintPath(Paint paint) {
        this.paint = paint;
    }

    public Paint getPaint() { return paint; }

    public int getColor() { return paint.getColor(); }

    public void setColor(int color) {
        paint.setColor(color);
    }

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

    public float getLeft() { return left; }
    public float getRight() { return right; }
    public float getBottom() { return bottom; }
    public float getTop() { return top; }
}