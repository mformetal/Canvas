package miles.scribble.ui.drawing;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.SystemClock;

import java.util.ArrayList;

/**
 * Created by mbpeele on 11/23/15.
 */
class Stroke extends ArrayList<CanvasPoint> {

    private static final float TOLERANCE = 5f;

    public Paint paint;

    public Stroke(Paint paint) {
        super();
        this.paint = new Paint(paint);
    }

    public CanvasPoint peek() {
        return get(size() - 1);
    }

    public void addPoint(float x, float y, Canvas canvas, Paint paint) {
        CanvasPoint nextPoint;
        if (isEmpty()) {
            nextPoint = new CanvasPoint(x, y, SystemClock.currentThreadTimeMillis());

            paint.setStrokeWidth(paint.getStrokeWidth() / 2);
            canvas.drawPoint(x, y, paint);
            add(nextPoint);
            paint.setStrokeWidth(paint.getStrokeWidth() * 2);
        } else {
            CanvasPoint prevPoint = peek();

            if (Math.abs(prevPoint.x - x) < TOLERANCE && Math.abs(prevPoint.y - y) < TOLERANCE) {
                return;
            }

            nextPoint = new CanvasPoint(x, y, SystemClock.currentThreadTimeMillis());

            add(nextPoint);
            draw(prevPoint, nextPoint, canvas, paint);
        }
    }

    public void draw(CanvasPoint previous, CanvasPoint next, Canvas canvas, Paint paint) {
        canvas.drawLine(previous.x, previous.y, next.x, next.y, paint);
    }
}
