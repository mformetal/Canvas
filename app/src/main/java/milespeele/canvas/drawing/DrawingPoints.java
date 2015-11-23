package milespeele.canvas.drawing;

import android.graphics.Paint;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Stack;

import milespeele.canvas.util.Logg;

/**
 * Created by mbpeele on 9/26/15.
 */
public class DrawingPoints extends ArrayList<DrawingPoint> {

    private Paint restorePaint;

    public DrawingPoints(DrawingPoints other) {
        super(other);
        restorePaint = new Paint(other.getRestorePaint());
    }

    public DrawingPoints(Paint paint) {
        super();
        restorePaint = new Paint(paint);
    }

    public DrawingPoint peek() {
        return get(size() - 1);
    }

    @Override
    public void clear() {
        super.clear();
        restorePaint.reset();
    }

    public Paint getRestorePaint() { return restorePaint; }

    public void setRestorePaint(Paint paint) { restorePaint = new Paint(paint); }
}
