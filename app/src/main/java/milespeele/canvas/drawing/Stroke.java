package milespeele.canvas.drawing;

import android.graphics.Paint;

import java.util.ArrayList;

/**
 * Created by mbpeele on 11/23/15.
 */
class Stroke extends ArrayList<CanvasPoint> {

    public Paint paint;

    public Stroke(Paint paint) {
        super();
        this.paint = new Paint(paint);
    }

    public CanvasPoint peek() {
        return get(size() - 1);
    }
}
