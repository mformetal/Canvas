package milespeele.canvas.drawing;

import android.graphics.Paint;

import com.esotericsoftware.kryo.serializers.FieldSerializer;

import java.util.ArrayList;

import milespeele.canvas.util.SerializablePaint;

/**
 * Created by mbpeele on 11/23/15.
 */
public class DrawingPoints extends ArrayList<DrawingPoint> {

    public float lastWidth = 1, lastVelocity = 1;
    public SerializablePaint redrawPaint;

    public DrawingPoints() {
        redrawPaint = new SerializablePaint();
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

    public DrawingPoint peek() {
        return get(size() - 1);
    }

    @Override
    public void clear() {
        super.clear();
        redrawPaint.reset();
        lastWidth = 1;
        lastVelocity = 1;
    }
}
