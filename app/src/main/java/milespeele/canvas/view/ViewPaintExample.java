package milespeele.canvas.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;

import java.util.Random;

import butterknife.ButterKnife;
import milespeele.canvas.R;
import milespeele.canvas.paint.PaintStyles;
import milespeele.canvas.util.Logg;

/**
 * Created by Miles Peele on 7/26/2015.
 */
public class ViewPaintExample extends View {

    private Paint examplePaint;
    private Path examplePath;
    private Paint borderPaint;

    public ViewPaintExample(Context context) {
        super(context);
        init(context, null);
    }

    public ViewPaintExample(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public ViewPaintExample(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public ViewPaintExample(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        examplePath = new Path();

        borderPaint = PaintStyles.normalPaint(Color.GRAY, 5f);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawColor(Color.BLACK);
        drawBorder(canvas);
        drawExamplePaint(canvas);
    }

    private void drawExamplePaint(Canvas canvas) {
        if (examplePaint != null) {
            examplePath.moveTo(0, canvas.getHeight() / 2);
            examplePath.lineTo(canvas.getWidth(), canvas.getHeight() / 2);
            canvas.drawPath(examplePath, examplePaint);
        }
    }

    private void drawBorder(Canvas canvas) {
        canvas.drawLine(0, 0, canvas.getWidth(), 0, borderPaint);
        canvas.drawLine(0, canvas.getHeight(), canvas.getWidth(), canvas.getHeight(), borderPaint);
        canvas.drawLine(0, 0, 0, canvas.getHeight(), borderPaint);
        canvas.drawLine(canvas.getWidth(), 0, canvas.getWidth(), canvas.getHeight(), borderPaint);
    }

    public void setPaint(Paint paint, int color) {
        examplePaint = paint;
        invalidate();
    }
}
