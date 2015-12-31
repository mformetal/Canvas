package milespeele.canvas.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.SystemClock;
import android.support.v4.view.MotionEventCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.LinearLayout;

import java.util.Calendar;

import milespeele.canvas.util.PaintStyles;
import milespeele.canvas.util.ViewUtils;

/**
 * Created by mbpeele on 11/9/15.
 */
public class ViewRippleLinearLayout extends LinearLayout {

    private Paint ripplePaint;
    private AnimatorSet animatorSet;

    private float radius, cx, cy;

    public ViewRippleLinearLayout(Context context) {
        super(context);
        init();
    }

    public ViewRippleLinearLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ViewRippleLinearLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public ViewRippleLinearLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        ripplePaint = PaintStyles.normal(Color.WHITE, 5f);
        ripplePaint.setStyle(Paint.Style.FILL_AND_STROKE);
        ripplePaint.setFlags(Paint.ANTI_ALIAS_FLAG);

        animatorSet = new AnimatorSet();
        animatorSet.setDuration(1000);

        setWillNotDraw(false);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int actionMasked = MotionEventCompat.getActionMasked(event);

        switch (event.getAction() & actionMasked) {
            case MotionEvent.ACTION_DOWN: {
                cx = event.getX();
                cy = event.getY();
                ripple();
                break;
            }
        }

        return super.onTouchEvent(event);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawCircle(cx, cy, radius, ripplePaint);
    }

    private void ripple() {
        if (!animatorSet.isRunning()) {
            ObjectAnimator rad = ObjectAnimator.ofFloat(this, "radius", 0, getMeasuredWidth());
            ObjectAnimator alpha = ObjectAnimator.ofObject(ripplePaint, ViewUtils.ALPHA,
                    new ArgbEvaluator(), ripplePaint.getAlpha(), 0);

            animatorSet.playTogether(rad, alpha);
            animatorSet.setDuration(500);
            animatorSet.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    ripplePaint.setAlpha(255);
                    radius = 0;
                }
            });
            animatorSet.start();
        }
    }

    public float getRadius() {
        return radius;
    }

    public void setRadius(float radius) {
        this.radius = radius;
        invalidate();
    }
}
