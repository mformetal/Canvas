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
 * Created by milespeele on 7/13/15.
 */
public class ViewBrushPickerSize extends View {

    private Paint paint;
    private Path path;

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
        paint = PaintStyles.normalPaint(Color.WHITE, 5f);

        path = new Path();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        int startX = w / 10;
        int endX = w - startX;
        path.moveTo(startX, h / 2);
        path.cubicTo(endX / 8, h / 4,
                Math.round(endX * .375), h / 4,
                endX / 2, h / 2);
        path.cubicTo(Math.round(endX * .675), Math.round(h * .75),
                Math.round(endX * .875), Math.round(h * .75),
                endX, h / 2);
    }

    public void onThicknessChanged(float thickness) {
        paint.setStrokeWidth(thickness);
        invalidate();
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawPath(path, paint);
    }
}
