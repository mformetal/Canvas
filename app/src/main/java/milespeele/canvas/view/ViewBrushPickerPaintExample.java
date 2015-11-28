package milespeele.canvas.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;

import milespeele.canvas.paint.PaintStyles;

/**
 * Created by Miles Peele on 7/26/2015.
 */
public class ViewBrushPickerPaintExample extends View {

    private Paint examplePaint;
    private Path examplePath;
    private Paint borderPaint;

    public ViewBrushPickerPaintExample(Context context) {
        super(context);
        init(context, null);
    }

    public ViewBrushPickerPaintExample(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public ViewBrushPickerPaintExample(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public ViewBrushPickerPaintExample(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        setLayerType(LAYER_TYPE_SOFTWARE, null);

        examplePath = new Path();

        borderPaint = PaintStyles.normal(Color.GRAY, 5f);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        int startX = w / 10;
        int endX = w - startX;
        examplePath.moveTo(startX, h / 2);
        examplePath.cubicTo(endX / 8, h / 4,
                endX * .375f, h / 4,
                endX / 2, h / 2);
        examplePath.cubicTo(endX * .675f, h * .75f,
                endX * .875f, h * .75f,
                endX, h / 2);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawBorder(canvas);
        drawExamplePaint(canvas);
    }

    private void drawExamplePaint(Canvas canvas) {
        if (examplePaint != null) {
            canvas.drawPath(examplePath, examplePaint);
        }
    }

    private void drawBorder(Canvas canvas) {
        canvas.drawRect(0, 0, canvas.getWidth(), canvas.getHeight(), borderPaint);
    }

    public void setPaint(Paint paint) {
        examplePaint = paint;
        examplePaint.setColor(Color.WHITE);
        invalidate();
    }

    public Paint getExamplePaint() {
        return examplePaint;
    }
}
