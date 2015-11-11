package milespeele.canvas.view;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.v4.view.MotionEventCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.LinearLayout;

import java.util.Calendar;

import milespeele.canvas.paint.PaintStyles;
import milespeele.canvas.util.AbstractAnimatorListener;
import milespeele.canvas.util.ViewUtils;

/**
 * Created by mbpeele on 11/9/15.
 */
public class ViewRippleLinearLayout extends LinearLayout {

    private Paint ripplePaint;
    private AnimatorSet animatorSet;

    private float radius, cx, cy;
    private long startClickTime;
    private final static long MAX_CLICK_DURATION = 200;

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
                startClickTime = Calendar.getInstance().getTimeInMillis();
                break;
            }

            case MotionEvent.ACTION_UP: {
                long clickDuration = Calendar.getInstance().getTimeInMillis() - startClickTime;
                if (clickDuration < MAX_CLICK_DURATION) {
                    if (!animatorSet.isRunning()) {
                        cx = event.getX();
                        cy = event.getY();
                        ripple();
                    }
                }
            }
        }

        return super.onTouchEvent(event);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (radius != 0) {
            canvas.drawCircle(cx, cy, radius, ripplePaint);
        }
    }

    private void ripple() {
        if (!animatorSet.isRunning()) {
            ObjectAnimator rad = ObjectAnimator.ofFloat(this, "radius", 0, getMeasuredWidth())
                    .setDuration(1000);

            ObjectAnimator alpha =  ObjectAnimator.ofObject(ripplePaint, ViewUtils.ALPHA,
                    new ArgbEvaluator(), ripplePaint.getAlpha(), 0)
                    .setDuration(1000);

            animatorSet.playTogether(rad, alpha);
            animatorSet.addListener(new AbstractAnimatorListener() {
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
