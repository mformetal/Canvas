package milespeele.canvas.drawing;

import android.graphics.Paint;

import java.io.EOFException;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;
import java.util.ArrayList;

import milespeele.canvas.util.Logg;
import milespeele.canvas.util.SerializablePaint;

/**
 * Created by mbpeele on 11/23/15.
 */
public class DrawingPoints extends ArrayList<DrawingPoint> implements Externalizable {

    private float lastWidth = 1, lastVelocity = 1;
    private SerializablePaint redrawPaint;

    public DrawingPoints() {

    }

    public DrawingPoints(Paint paint) {
        super();
        redrawPaint = new SerializablePaint(paint);
    }

    public DrawingPoints(DrawingPoints other) {
        super(other);
        lastWidth = other.lastWidth;
        lastVelocity = other.lastVelocity;
        redrawPaint = new SerializablePaint(other.redrawPaint);
    }

    public DrawingPoint getLast() {
        return get(size() - 1);
    }

    @Override
    public void clear() {
        super.clear();
        redrawPaint.reset();
        lastWidth = 1;
        lastVelocity = 1;
    }

    public float getLastWidth() {
        return lastWidth;
    }

    public void setLastWidth(float lastWidth) {
        this.lastWidth = lastWidth;
    }

    public float getLastVelocity() {
        return lastVelocity;
    }

    public void setLastVelocity(float lastVelocity) {
        this.lastVelocity = lastVelocity;
    }

    public Paint getRedrawPaint() {
        return redrawPaint;
    }

    public void setRedrawPaint(Paint paint) {
        redrawPaint.set(paint);
    }

    @Override
    public void readExternal(ObjectInput input) throws IOException, ClassNotFoundException {
        lastWidth = input.readFloat();
        lastVelocity = input.readFloat();
        redrawPaint = (SerializablePaint) input.readObject();
        int size = input.readInt();
        for (int x = 0; x < size; x++) {
            add((DrawingPoint) input.readObject());
        }
    }

    @Override
    public void writeExternal(ObjectOutput output) throws IOException {
        output.writeFloat(lastWidth);
        output.writeFloat(lastVelocity);
        output.writeObject(redrawPaint);
        output.writeInt(size());
        for (DrawingPoint point: this) {
            output.writeObject(point);
        }
    }
}
