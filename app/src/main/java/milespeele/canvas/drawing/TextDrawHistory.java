package milespeele.canvas.drawing;

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
}
