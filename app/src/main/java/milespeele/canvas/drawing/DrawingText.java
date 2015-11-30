package milespeele.canvas.drawing;

import android.graphics.Paint;

import java.util.ArrayList;

/**
 * Created by mbpeele on 11/29/15.
 */
public class DrawingText {

    public CharSequence text;
    public float x;
    public float y;
    public float scale;
    public Paint textPaint;

    public DrawingText(CharSequence text, float x, float y, float scale, Paint textPaint) {
        this.text = text;
        this.x = x;
        this.y = y;
        this.scale = scale;
        this.textPaint = new Paint(textPaint);
    }

}
