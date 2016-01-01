package milespeele.canvas.drawing;

import android.animation.ObjectAnimator;
import android.graphics.Canvas;
import android.graphics.Paint;

import java.io.EOFException;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Stack;

import milespeele.canvas.util.Logg;
import milespeele.canvas.util.ViewUtils;

/**
 * Created by mbpeele on 11/29/15.
 */
public class DrawingHistory extends Stack<Object> {

    public DrawingHistory() {}

    @Override
    public Object push(Object object) {
        if (object instanceof DrawingPoints) {
            return super.push(new DrawingPoints((DrawingPoints) object));
        } else if (object instanceof DrawingText) {
            return super.push(object);
        }

        throw(new UnsupportedOperationException());
    }

    @SuppressWarnings("ResourceType")
    public void redraw(Canvas canvas) {
        for (Object object: this) {
            if (object instanceof DrawingPoints) {
                DrawingPoints points = (DrawingPoints) object;
                canvas.drawLines(points.redrawPts, points.redrawPaint);
            } else if (object instanceof DrawingText) {
                DrawingText texts = (DrawingText) object;
                canvas.save();
                canvas.concat(texts.matrix);
                texts.text.draw(canvas);
                canvas.restore();
            }
        }
    }
}
