package milespeele.canvas.drawing;

import android.graphics.Canvas;
import android.graphics.Matrix;
import android.text.TextPaint;

/**
 * Created by mbpeele on 1/4/16.
 */
class TextDrawHistory {

    public String text;
    public float[] matrixValues;
    public TextPaint paint;

    public TextDrawHistory(String text, float[] matrixValues, TextPaint paint) {
        this.text = text;
        this.matrixValues = matrixValues;
        this.paint = new TextPaint(paint);
    }

    public void draw(Canvas canvas, Matrix matrix, int width, int height) {
        float[] prevMatrixValues = new float[9];
        matrix.getValues(prevMatrixValues);

        matrix.setValues(matrixValues);

        canvas.save();
        canvas.concat(matrix);
        canvas.drawText(text,
                width / 2 - paint.measureText(text) / 2,
                height / 2,
                paint);
        canvas.restore();

        matrix.setValues(prevMatrixValues);
    }
}
