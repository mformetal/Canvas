package milespeele.canvas.drawing;

import java.util.ArrayList;
import java.util.Stack;

import milespeele.canvas.util.Logg;

/**
 * Created by mbpeele on 9/26/15.
 */
public class DrawingPoints extends ArrayList<DrawingPoint> {

    public float left, top, right, bottom;

    public DrawingPoints() {
        super();
    }

    public DrawingPoints(DrawingPoints other) {
        super(other);
        left = other.left;
        top = other.top;
        right = other.right;
        bottom = other.bottom;
    }

    public DrawingPoint peek() {
        return get(size() - 1);
    }

    @Override
    public boolean add(DrawingPoint object) {
        setContainedRect(object);
        return super.add(object);
    }

    private void setContainedRect(DrawingPoint point) {
        if (size() == 0) {
            left = point.x;
            right = point.x;
            top = point.y;
            bottom = point.y;
        } else {
            if (left < point.x) {
                right = point.x;
            } else {
                right = left;
                left = point.x;
            }

            if (top < point.y) {
                bottom = point.y;
            } else {
                bottom = top;
                top = point.y;
            }
        }
    }

    public void printPoints() {
        Logg.log(left, right, top, bottom);
    }
}
