package milespeele.canvas.drawing;

import android.graphics.Matrix;
import android.graphics.Paint;
import android.text.DynamicLayout;

import java.util.ArrayList;

/**
 * Created by mbpeele on 11/29/15.
 */
public class DrawingText {

    public DynamicLayout text;
    public Matrix matrix;
    public Paint textPaint;

    public DrawingText(DynamicLayout text, Matrix matrix, Paint textPaint) {
        this.text = text;
        this.matrix = matrix;
        this.textPaint = new Paint(textPaint);
    }

}
