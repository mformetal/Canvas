package milespeele.canvas.drawing;

import android.graphics.Canvas;
import android.graphics.Matrix;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;

/**
 * Created by mbpeele on 1/4/16.
 */
class TextDrawHistory {

    public CharSequence text;
    public float[] matrixValues;
    public TextPaint paint;

    public TextDrawHistory(CharSequence text, float[] matrixValues, TextPaint paint) {
        this.text = text;
        this.matrixValues = matrixValues;
        this.paint = new TextPaint(paint);
    }

    public void draw(Canvas canvas, Matrix matrix) {
        StaticLayout layout = new StaticLayout(text, paint, canvas.getWidth(),
                Layout.Alignment.ALIGN_CENTER, 1, 1, false);

        float[] prevMatrixValues = new float[9];
        matrix.getValues(prevMatrixValues);

        matrix.setValues(matrixValues);

        canvas.save();
        canvas.concat(matrix);
        layout.draw(canvas);
        canvas.restore();

        matrix.setValues(prevMatrixValues);
    }
}
