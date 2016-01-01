package milespeele.canvas.drawing;

import android.graphics.Matrix;
import android.graphics.Paint;
import android.text.DynamicLayout;

/**
 * Created by mbpeele on 11/29/15.
 */
public class DrawingText {

    public DynamicLayout text;
    public Matrix matrix;
    public Paint paint;

    public DrawingText(DynamicLayout text, Matrix matrix, Paint textPaint) {
        this.text = text;
        this.matrix = matrix;
        this.paint = new Paint(textPaint);
    }

}
