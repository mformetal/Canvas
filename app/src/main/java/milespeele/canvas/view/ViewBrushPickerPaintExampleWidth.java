package milespeele.canvas.view;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathEffect;
import android.graphics.PathMeasure;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;

import java.util.ArrayList;

import milespeele.canvas.R;
import milespeele.canvas.paint.PaintStyles;
import milespeele.canvas.util.AbstractAnimatorListener;
import milespeele.canvas.util.Logg;

/**
 * Created by milespeele on 7/13/15.
 */
public class ViewBrushPickerPaintExampleWidth extends View {

    private final static Interpolator INTERPOLATOR = new AccelerateDecelerateInterpolator();

    private Paint paint;
    private Paint rectPaint;
    private Path path;
    private ObjectAnimator reveal;

    private float rectWidth;

    public ViewBrushPickerPaintExampleWidth(Context context) {
        super(context);
        init();
    }

    public ViewBrushPickerPaintExampleWidth(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ViewBrushPickerPaintExampleWidth(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public ViewBrushPickerPaintExampleWidth(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
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

        rectPaint = PaintStyles.normalPaint(getResources().getColor(R.color.primary_dark), 5f);
        rectPaint.setStyle(Paint.Style.FILL);
        rectPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC));

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

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawPath(path, paint);

        if (reveal != null) {
            canvas.drawRect(rectWidth, 0, canvas.getWidth(), canvas.getHeight(), rectPaint);
        }
    }

    public void onThicknessChanged(float thickness) {
        paint.setStrokeWidth(thickness);
        invalidate();
    }

    public void changePaint(Paint newPaint) {
        float paintThickness = paint.getStrokeWidth();
        paint.set(newPaint);
        paint.setStrokeWidth(paintThickness);
        reveal = ObjectAnimator.ofFloat(this, "rectWidth", 0, getMeasuredWidth());
        reveal.setDuration(1000);
        reveal.setInterpolator(INTERPOLATOR);
        reveal.start();
    }

    public float getRectWidth() {
        return rectWidth;
    }

    public void setRectWidth(float rectWidth) {
        this.rectWidth = rectWidth;
        invalidate();
    }
}
