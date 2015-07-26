package milespeele.canvas.paint;

import android.graphics.Paint;
import android.graphics.Path;

/**
 * Created by milespeele on 7/9/15.
 */
public class PaintPath extends Path {

    private Paint paint;
    private int color;

    public PaintPath(Paint paint) {
        this.paint = paint;
        this.color = paint.getColor();
    }

    public Paint getPaint() { return paint; }

    public int getColor() { return color; }

    public void setColor(int color) { paint.setColor(color); }
}