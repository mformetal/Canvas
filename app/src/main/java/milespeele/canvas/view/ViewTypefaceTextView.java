package milespeele.canvas.view;

import android.animation.Animator;
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

import milespeele.canvas.R;
import milespeele.canvas.util.PaintStyles;
import milespeele.canvas.util.TextUtils;

/**
 * Created by mbpeele on 9/2/15.
 */
public class ViewTypefaceTextView extends TextView {

    private Paint borderPaint;

    private int borderColor;

    public ViewTypefaceTextView(Context context) {
        super(context);
        init(null);
    }

    public ViewTypefaceTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public ViewTypefaceTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public ViewTypefaceTextView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        if (isInEditMode()) {
            return;
        }

        setTypeface(TextUtils.getStaticTypeFace(getContext(), "Roboto.ttf"));

        if (attrs != null) {
            TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.ViewTypefaceTextView);
            if (typedArray.getBoolean(R.styleable.ViewTypefaceTextView_shouldDrawBorder, false)) {
                borderColor = typedArray.getColor(R.styleable.ViewTypefaceTextView_borderColor, Color.GRAY);
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

    public void animateTextColor(int color, long duration, Animator.AnimatorListener listener) {
        ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(),
                getCurrentTextColor(), color);
        colorAnimation.addUpdateListener(animator -> setTextColor((Integer) animator.getAnimatedValue()));
        colorAnimation.addListener(listener);
        colorAnimation.setDuration(duration);
        colorAnimation.start();
    }
}
