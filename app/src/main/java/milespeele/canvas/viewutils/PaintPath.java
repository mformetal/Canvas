package milespeele.canvas.viewutils;

import android.graphics.Path;

/**
 * Created by milespeele on 7/9/15.
 */
public class PaintPath extends Path {

    private int color;

    public PaintPath(int color) {
        this.color = color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public int getColor() {
        return color;
    }

}
