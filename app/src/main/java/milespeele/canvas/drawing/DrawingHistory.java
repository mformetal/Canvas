package milespeele.canvas.drawing;

import android.graphics.Canvas;
import android.graphics.Paint;

import java.util.Stack;

/**
 * Created by mbpeele on 11/29/15.
 */
public class DrawingHistory extends Stack<Object> {

    @Override
    public Object push(Object object) {
        if (object instanceof DrawingPoints) {
            return super.push(new DrawingPoints((DrawingPoints) object));
        } else if (object instanceof DrawingText) {
            return super.push(object);
        }

        throw(new UnsupportedOperationException());
    }

    public void redraw(Canvas canvas) {
        for (Object object: this) {
            if (object instanceof DrawingPoints) {
                DrawingPoints points = (DrawingPoints) object;
                Paint redraw = points.getRedrawPaint();
                for (DrawingPoints.DrawingPoint point: points) {
                    redraw.setStrokeWidth(point.width);
                    redraw.setColor(point.color);
                    canvas.drawPoint(point.x, point.y, redraw);
                }
            } else if (object instanceof DrawingText) {
                DrawingText texts = (DrawingText) object;
                canvas.save();
                canvas.scale(texts.scale, texts.scale);
                canvas.drawText(texts.text, texts.x, texts.y, texts.textPaint);
                canvas.restore();
            }
        }
    }
}
