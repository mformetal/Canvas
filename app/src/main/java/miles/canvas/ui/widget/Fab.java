package miles.canvas.ui.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.MotionEventCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SoundEffectConstants;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

import miles.canvas.R;
import miles.canvas.util.ViewUtils;

public class Fab extends FloatingActionButton {

    private AnimatorSet pulse;
    private Paint ripplePaint;
    private AnimatorSet animatorSet;

    private boolean isScaledUp = false;
    private boolean isScaling = false;
    private float cx, cy, radius;
    private final static int RIPPLE_DURATION = 500;

    public Fab(Context context) {
        super(context);
        init(null);
    }

    public Fab(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public Fab(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        ripplePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        ripplePaint.setColor(getResources().getColor(R.color.half_opacity_gray));

        animatorSet = new AnimatorSet();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawCircle(cx, cy, radius, ripplePaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction() & MotionEventCompat.getActionMasked(event)) {
            case MotionEvent.ACTION_DOWN:
                cx = event.getX();
                cy = event.getY();
                ripple();
                break;
        }
        return super.onTouchEvent(event);
    }

    @Override
    public boolean performClick() {
        boolean hasListener = super.performClick();
        if (!hasListener) {
            playSoundEffect(SoundEffectConstants.CLICK);
        }
        return hasListener;
    }

    public void toggleScaled() {
        if (!isScaling) {
            if (isScaledUp) {
                scaleDown();
            } else {
                scaleUp();
            }
        }
    }

    public boolean isScaledUp() { return isScaledUp; }

    public void scaleDown() {
        ObjectAnimator scaleDown = ObjectAnimator.ofPropertyValuesHolder(this,
                PropertyValuesHolder.ofFloat(View.SCALE_X, 1f),
                PropertyValuesHolder.ofFloat(View.SCALE_Y, 1f))
                .setDuration(350);
        scaleDown.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                setBackgroundTintList(getResources().getColorStateList(android.R.color.white));
                isScaling = true;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                isScaling = false;
                isScaledUp = false;
            }
        });
        scaleDown.start();
    }

    public void scaleUp() {
        ObjectAnimator scaleUp = ObjectAnimator.ofPropertyValuesHolder(this,
                PropertyValuesHolder.ofFloat(View.SCALE_X, 1f, 1.1f),
                PropertyValuesHolder.ofFloat(View.SCALE_Y, 1f, 1.1f))
                .setDuration(350);
        scaleUp.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                setBackgroundTintList(getResources().getColorStateList(android.R.color.black));
                isScaling = true;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                isScaling = false;
                isScaledUp = true;
            }
        });
        scaleUp.start();
    }

    public void startSaveAnimation() {
        if (pulse == null) {
            ObjectAnimator scaleX = ObjectAnimator.ofFloat(this, View.SCALE_X, 1f, .75f);
            scaleX.setRepeatCount(ValueAnimator.INFINITE);
            scaleX.setRepeatMode(ValueAnimator.REVERSE);

            ObjectAnimator scaleY = ObjectAnimator.ofFloat(this, View.SCALE_Y, 1f, .75f);
            scaleY.setRepeatCount(ValueAnimator.INFINITE);
            scaleY.setRepeatMode(ValueAnimator.REVERSE);

            pulse = new AnimatorSet();
            pulse.playTogether(scaleX, scaleY);
            pulse.setDuration(300);
            pulse.start();
        } else {
            if (!pulse.isRunning()) {
                pulse.start();
            }
        }
    }

    public void stopSaveAnimation() {
        if (pulse != null) {
            ObjectAnimator.ofPropertyValuesHolder(this,
                    PropertyValuesHolder.ofFloat(View.SCALE_X, 1f),
                    PropertyValuesHolder.ofFloat(View.SCALE_Y, 1f)).start();
            pulse.end();
        }
    }

    public void ripple() {
        if (!animatorSet.isRunning()) {
            ObjectAnimator rad = ObjectAnimator.ofFloat(this, RIPPLE, 0, getMeasuredWidth() / 2);
            ObjectAnimator alpha =  ObjectAnimator.ofObject(ripplePaint, ViewUtils.ALPHA,
                    new ArgbEvaluator(), ripplePaint.getAlpha(), 0);

            animatorSet.playTogether(rad, alpha);
            animatorSet.setDuration(RIPPLE_DURATION);
            animatorSet.setInterpolator(new AccelerateDecelerateInterpolator());
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

    private static ViewUtils.FloatProperty<Fab> RIPPLE
            = new ViewUtils.FloatProperty<Fab>("ripple") {
        @Override
        public void setValue(Fab object, float value) {
            object.setRadius(value);
        }

        @Override
        public Float get(Fab object) {
            return object.getRadius();
        }
    };
}