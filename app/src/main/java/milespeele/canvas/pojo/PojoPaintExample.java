package milespeele.canvas.pojo;

import android.graphics.Color;
import android.graphics.Paint;

import java.util.ArrayList;

/**
 * Created by Miles Peele on 9/17/2015.
 */
public class PojoPaintExample {

    private String which;
    private Paint paint;
    private int color;

    public PojoPaintExample(String which, Paint paint) {
        this.which = which;
        this.paint = paint;
    }

    public String getPaintName() {
        return which;
    }

    public Paint getPaint() { return paint; }

    public void setColorForText(int colorForText) { color = colorForText; }

    public int getColorForText() {
        return (color != 0) ? color : Color.WHITE; }

}
