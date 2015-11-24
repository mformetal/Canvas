package milespeele.canvas.drawing;

import java.util.Stack;

import milespeele.canvas.util.Logg;

/**
 * Created by mbpeele on 11/24/15.
 */
public class DrawingHistory extends Stack<DrawingPoints> {

    @Override
    public DrawingPoints push(DrawingPoints object) {
        DrawingPoints drawingPoints = new DrawingPoints(object);
        add(drawingPoints);
        return drawingPoints;
    }

}
