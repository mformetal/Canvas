package milespeele.canvas.drawing;

import android.graphics.Paint;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Stack;

import milespeele.canvas.util.Logg;

/**
 * Created by mbpeele on 9/26/15.
 */
public class DrawingPoints extends ArrayList<DrawingPoint> {

    public Paint paint;

    public DrawingPoints(DrawingPoints other) {
        super(other);
        paint = new Paint(other.paint);
    }

    public DrawingPoints(Paint other) {
        super();
        paint = new Paint(other);
    }

    public DrawingPoint peek() {
        return get(size() - 1);
    }

}
