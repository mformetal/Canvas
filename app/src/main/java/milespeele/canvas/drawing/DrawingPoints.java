package milespeele.canvas.drawing;

import java.util.ArrayList;
import java.util.Stack;

import milespeele.canvas.util.Logg;

/**
 * Created by mbpeele on 9/26/15.
 */
public class DrawingPoints extends ArrayList<DrawingPoint> {

    public DrawingPoints() {
        super();
    }

    public DrawingPoints(DrawingPoints other) {
        super(other);
    }

    public DrawingPoint getLast() {
        return get(size() - 1);
    }

}
