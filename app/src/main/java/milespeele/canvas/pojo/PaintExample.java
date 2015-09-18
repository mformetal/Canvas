package milespeele.canvas.pojo;

import android.graphics.Paint;

import java.util.ArrayList;

/**
 * Created by Miles Peele on 9/17/2015.
 */
public class PaintExample {

    private String which;
    private Paint paint;

    public PaintExample(String which, Paint paint) {
        this.which = which;
        this.paint = paint;
    }

    public String getPaintName() {
        return which;
    }

    public Paint getPaint() {
        return paint;
    }

}
