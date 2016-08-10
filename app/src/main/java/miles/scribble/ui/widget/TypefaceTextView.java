package miles.scribble.ui.widget;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.TextView;

import miles.scribble.R;
import miles.scribble.util.PaintStyles;
import miles.scribble.util.TextUtils;

/**
 * Created by mbpeele on 9/2/15.
 */
public class TypefaceTextView extends TextView {

    private Paint borderPaint;

    private int borderColor;

    public TypefaceTextView(Context context) {
        super(context);
        init(null);
    }

    public TypefaceTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public TypefaceTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public TypefaceTextView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        if (isInEditMode()) {
            return;
        }

        setTypeface(TextUtils.getStaticTypeFace(getContext()));

        if (attrs != null) {
            TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.TypefaceTextView);
            if (typedArray.getBoolean(R.styleable.TypefaceTextView_shouldDrawBorder, false)) {
                borderColor = typedArray.getColor(R.styleable.TypefaceTextView_borderColor, Color.GRAY);
                borderPaint = PaintStyles.normal(borderColor, 5f);
            }
            typedArray.recycle();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawBorder(canvas);
    }

    private void drawBorder(Canvas canvas) {
        if (borderPaint != null) {
            canvas.drawRect(0, 0, canvas.getWidth(), canvas.getHeight(), borderPaint);
        }
    }

    public String getTextAsString() {
        return getText().toString();
    }

    public ValueAnimator animateTextColor(int color, long duration) {
        ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(),
                getCurrentTextColor(), color);
        colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                setTextColor((Integer) animation.getAnimatedValue());
            }
        });
        colorAnimation.setDuration(duration);
        return colorAnimation;
    }
}
