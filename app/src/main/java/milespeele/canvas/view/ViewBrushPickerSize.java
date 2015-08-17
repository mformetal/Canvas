package milespeele.canvas.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by milespeele on 7/13/15.
 */
public class ViewBrushPickerSize extends View {

    private Paint curPaint;

    public ViewBrushPickerSize(Context context) {
        super(context);
        init();
    }

    public ViewBrushPickerSize(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ViewBrushPickerSize(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public ViewBrushPickerSize(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = getMeasuredWidth();
        setMeasuredDimension(width, width / 5);
    }


    private void init() {
        curPaint = new Paint();
        curPaint.setAntiAlias(true);
        curPaint.setColor(Color.WHITE);
        curPaint.setStyle(Paint.Style.STROKE);
            curPaint.setStrokeJoin(Paint.Join.ROUND);
        curPaint.setStrokeCap(Paint.Cap.ROUND);
    }

    public void onValuesChanged(float thickness) {
        curPaint.setStrokeWidth(thickness);
        invalidate();
    }

    public void onThicknessChanged(float thickness) {
        curPaint.setStrokeWidth(thickness);
        invalidate();
    }

    public void onAlphaChanged(int alpha) {
        curPaint.setAlpha(alpha);
        invalidate();
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawLine(getPaddingLeft() + getPaddingRight(), canvas.getHeight() / 2,
                canvas.getWidth() - getPaddingLeft() - getPaddingRight(), canvas.getHeight() / 2,
                curPaint);
    }
}
