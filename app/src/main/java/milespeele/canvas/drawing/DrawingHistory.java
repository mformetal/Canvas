package milespeele.canvas.drawing;

import android.graphics.Canvas;
import android.graphics.Paint;

import java.io.EOFException;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Stack;

import milespeele.canvas.util.Logg;

/**
 * Created by mbpeele on 11/29/15.
 */
public class DrawingHistory extends Stack<Object> implements Externalizable {

    public DrawingHistory() {

    }

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
                for (DrawingPoint point: points) {
                    redraw.setStrokeWidth(point.width);
                    redraw.setColor(point.color);
                    canvas.drawPoint(point.x, point.y, redraw);
                }
            } else if (object instanceof DrawingText) {
                DrawingText texts = (DrawingText) object;
                canvas.save();
                canvas.scale(texts.scale, texts.scale);
                canvas.drawText((String) texts.text, texts.x, texts.y, texts.textPaint);
                canvas.restore();
            }
        }
    }

    @Override
    public void readExternal(ObjectInput input) throws IOException, ClassNotFoundException {
        int size = input.readInt();
        if (size > 0) {
            for (int x = 0; x < size; x++) {
                super.push(input.readObject());
            }
        }
    }

    @Override
    public void writeExternal(ObjectOutput output) throws IOException {
        int size = size();
        output.writeInt(size);
        if (size > 0) {
            for (Object object: this) {
                output.writeObject(object);
            }
        }
    }
}
