package milespeele.canvas.drawing;

import android.graphics.Point;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Stack;

import milespeele.canvas.util.Logg;

/**
 * Created by mbpeele on 9/28/15.
 */
public class DrawingHistory extends Stack<DrawingPoints> {

    public DrawingHistory() {
    }

    @Override
    public DrawingPoints push(DrawingPoints object) {
        DrawingPoints toAdd = new DrawingPoints(object);
        add(toAdd);
        return toAdd;
    }
}
