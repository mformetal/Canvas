package milespeele.canvas.view;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.support.design.widget.FloatingActionButton;
import android.util.AttributeSet;
import android.view.SoundEffectConstants;
import android.view.View;

import milespeele.canvas.R;
import milespeele.canvas.util.AbstractAnimatorListener;
import milespeele.canvas.util.Logg;

public class ViewFab extends FloatingActionButton {

    private String buttonText;

    private boolean isScaledUp = false;
    private boolean isScaling = false;

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
        if (attrs != null) {
            TypedArray typedArray = getResources().obtainAttributes(attrs, R.styleable.ViewFab);
            buttonText = typedArray.getString(R.styleable.ViewFab_text);
            typedArray.recycle();
        }
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

    public void scaleUp() {
        if (!isScaledUp) {
            setBackgroundTintList(getResources().getColorStateList(R.color.primary_light));
            ObjectAnimator scaleDown = ObjectAnimator.ofPropertyValuesHolder(this,
                    PropertyValuesHolder.ofFloat(View.SCALE_X, 1f),
                    PropertyValuesHolder.ofFloat(View.SCALE_Y, 1f))
                    .setDuration(350);
            scaleDown.addListener(new AbstractAnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {
                    isScaling = true;
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    isScaledUp = false;
                }
            });
        }
    }

    public void scaleDown() {
        if (isScaledUp) {
            setBackgroundTintList(getResources( ).getColorStateList(R.color.accent));
            ObjectAnimator scaleUp = ObjectAnimator.ofPropertyValuesHolder(this,
                    PropertyValuesHolder.ofFloat(View.SCALE_X, 1f, 1.1f),
                    PropertyValuesHolder.ofFloat(View.SCALE_Y, 1f, 1.1f))
                    .setDuration(350);
            scaleUp.addListener(new AbstractAnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {
                    isScaling = true;
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    isScaledUp = true;
                }
            });
        }
    }

    public void startSaveAnimation() {
    }

    public void stopSaveAnimation() {
    }

    public String getButtonText() {
        return (buttonText != null) ? buttonText : "";
    }
}