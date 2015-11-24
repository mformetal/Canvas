package milespeele.canvas.drawing;

import java.util.ArrayList;

/**
 * Created by mbpeele on 11/23/15.
 */
public class DrawingPoints extends ArrayList<DrawingPoint> {

    public float lastWidth, lastVelocity;

    public DrawingPoint getLast() {
        return get(size() - 1);
    }

    @Override
    public void clear() {
        super.clear();
        lastWidth = 0;
        lastVelocity = 0;
    }
}
