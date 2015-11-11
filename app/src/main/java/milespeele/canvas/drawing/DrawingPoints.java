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

    private static final int CAPACITY = 3000;

    private HashSet<DrawingPoint> set;

    public DrawingPoints(DrawingPoints other) {
        super(other);
        set = new HashSet<>(CAPACITY);
    }

    public DrawingPoints(Paint other) {
        super();
        set = new HashSet<>(CAPACITY);
    }

    public DrawingPoint peek() {
        return get(size() - 1);
    }

    public DrawingPoint first() { return get(0); }

    @Override
    public boolean add(DrawingPoint object) {
        set.add(object);
        return super.add(object);
    }

    @Override
    public boolean contains(Object object) {
        return set.contains(object);
    }
}
