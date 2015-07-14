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
public class ViewBrushSize extends View {

    private Paint curPaint;
    private PaintPath path;

    public ViewBrushSize(Context context) {
        super(context);
        init();
    }

    public ViewBrushSize(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ViewBrushSize(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public ViewBrushSize(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(100, 100);
    }


    private void init() {
        curPaint = new Paint();
        curPaint.setAntiAlias(true);
        curPaint.setColor(Color.WHITE);
        curPaint.setStyle(Paint.Style.STROKE);
            curPaint.setStrokeJoin(Paint.Join.ROUND);
        curPaint.setStrokeCap(Paint.Cap.ROUND);

        path = new PaintPath(curPaint);
    }

    public void onThicknessChanged(int thickness) {
        curPaint.setStrokeWidth((float) thickness);
        invalidate();
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        path.lineTo(0, getHeight());
        canvas.drawPath(path, curPaint);
    }
}
