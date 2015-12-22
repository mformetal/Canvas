package milespeele.canvas.view;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.support.design.widget.FloatingActionButton;
import android.util.AttributeSet;
import android.view.View;

import milespeele.canvas.R;
import milespeele.canvas.util.AbstractAnimatorListener;
import milespeele.canvas.util.Logg;

public class ViewFab extends FloatingActionButton {

    private AnimatorSet scaleUp;
    private AnimatorSet scaleDown;
    private AnimatorSet pulse;
    private String buttonText;

    private boolean isScaledUp = false;

    public ViewFab(Context context) {
        super(context);
        init(null);
    }

    public ViewFab(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public ViewFab(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        scaleUp = new AnimatorSet();
        scaleUp.playTogether(ObjectAnimator.ofFloat(this, "scaleX", 1f, 1.1f),
                ObjectAnimator.ofFloat(this, "scaleY", 1f, 1.1f));
        scaleUp.addListener(new AbstractAnimatorListener() {

            @Override
            public void onAnimationEnd(Animator animation) {
                isScaledUp = true;
            }
        });

        scaleDown = new AnimatorSet();
        scaleDown.playTogether(ObjectAnimator.ofFloat(this, "scaleX", 1),
                ObjectAnimator.ofFloat(this, "scaleY", 1));
        scaleDown.addListener(new AbstractAnimatorListener() {

            @Override
            public void onAnimationEnd(Animator animation) {
                isScaledUp = false;
            }
        });

        if (attrs != null) {
            TypedArray typedArray = getResources().obtainAttributes(attrs, R.styleable.ViewFab);
            buttonText = typedArray.getString(R.styleable.ViewFab_text);
            typedArray.recycle();
        }
    }

    public void toggleScaled() {
        if (!scaleUp.isRunning() || !scaleDown.isRunning()) {
            if (isScaledUp) {
                scaleDown();
            } else {
                scaleUp();
            }
        }
    }

    public boolean isScaledUp() { return isScaledUp; }

    public void scaleUp() {
        if (!isScaledUp) {
            setBackgroundTintList(getResources().getColorStateList(R.color.primary_light));
            scaleUp.start();
        }
    }

    public void scaleDown() {
        if (isScaledUp) {
            setBackgroundTintList(getResources().getColorStateList(R.color.accent));
            scaleDown.start();
        }
    }

    public void startPulse() {
        if (pulse == null) {
            pulse = new AnimatorSet();
            ObjectAnimator scaleX = ObjectAnimator.ofFloat(this, "scaleX", 1f, .5f);
            scaleX.setRepeatCount(ValueAnimator.INFINITE);
            scaleX.setRepeatMode(ValueAnimator.REVERSE);
            ObjectAnimator scaleY = ObjectAnimator.ofFloat(this, "scaleY", 1f, .5f);
            scaleY.setRepeatCount(ValueAnimator.INFINITE);
            scaleY.setRepeatMode(ValueAnimator.REVERSE);
            pulse.playTogether(scaleX, scaleY);
            pulse.setDuration(300);
            pulse.start();
        } else {
            if (!pulse.isRunning()) {
                 pulse.start();
            }
        }
    }

    public void stopPulse() {
        if (pulse != null) {
            AnimatorSet normalize = new AnimatorSet();
            ObjectAnimator scaleX = ObjectAnimator.ofFloat(this, "scaleX", 1f);
            ObjectAnimator scaleY = ObjectAnimator.ofFloat(this, "scaleY", 1f);
            normalize.playTogether(scaleX, scaleY);
            normalize.start();
            pulse.end();
        }
    }

    public String getButtonText() {
        return (buttonText != null) ? buttonText : "";
    }
}