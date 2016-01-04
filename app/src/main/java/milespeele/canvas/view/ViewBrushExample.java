package milespeele.canvas.view;

import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;

import milespeele.canvas.R;
import milespeele.canvas.util.Logg;
import milespeele.canvas.util.PaintStyles;

/**
 * Created by milespeele on 7/13/15.
 */
public class ViewBrushExample extends View {

    private final static Interpolator INTERPOLATOR = new AccelerateDecelerateInterpolator();

    private Paint paint, rectPaint;
    private Path path;
    private ObjectAnimator reveal;

    private float rectWidth;

    public ViewBrushExample(Context context) {
        super(context);
        init();
    }

    public ViewBrushExample(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ViewBrushExample(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public ViewBrushExample(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
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
        paint = new Paint();

        rectPaint = PaintStyles.normal(getResources().getColor(R.color.primary_dark), 5f);
        rectPaint.setStyle(Paint.Style.FILL);
        rectPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC));

        path = new Path();

        setWillNotDraw(false);
        setLayerType(LAYER_TYPE_SOFTWARE, null);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        int startX = w / 10;
        int endX = w - startX;
        path.moveTo(startX, h / 2);
        path.cubicTo(endX / 8, h / 4,
                endX * .375f, h / 4,
                endX / 2, h / 2);
        path.cubicTo(endX * .675f, h * .75f,
                endX * .875f, h * .75f,
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

    public Paint getPaint() {
        return paint;
    }

    public void setInitialPaint(Paint initialPaint) {
        paint.set(initialPaint);
        invalidate();
    }

    public void animatePaintChange(Paint newPaint) {
        paint.set(newPaint);

        reveal = ObjectAnimator.ofFloat(this, "rectWidth", 0, getMeasuredWidth());
        reveal.setDuration(1000);
        reveal.setInterpolator(INTERPOLATOR);
        reveal.start();

        invalidate();
    }

    public float getRectWidth() {
        return rectWidth;
    }

    public void setRectWidth(float rectWidth) {
        this.rectWidth = rectWidth;
        invalidate();
    }
}