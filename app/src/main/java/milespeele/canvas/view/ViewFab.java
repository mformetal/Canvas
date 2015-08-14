package milespeele.canvas.view;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.support.design.widget.FloatingActionButton;
import android.util.AttributeSet;

import milespeele.canvas.R;

public class ViewFab extends FloatingActionButton {

    private AnimatorSet scaleUp;
    private AnimatorSet scaleDown;
    private boolean isScaled = false;
    private boolean isScaling = false;

    public ViewFab(Context context) {
        super(context);
        init();
    }

    public ViewFab(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ViewFab(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        scaleUp = new AnimatorSet();
        scaleUp.playTogether(ObjectAnimator.ofFloat(this, "scaleX", 1f, 1.2f),
                ObjectAnimator.ofFloat(this, "scaleY", 1f, 1.2f));
        scaleUp.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                isScaling = true;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                isScaled = true;
                isScaling = false;
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });

        scaleDown = new AnimatorSet();
        scaleDown.playTogether(ObjectAnimator.ofFloat(this, "scaleX", 1),
                ObjectAnimator.ofFloat(this, "scaleY", 1));
        scaleDown.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                isScaling = true;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                isScaled = false;
                isScaling = false;
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
    }

    public void toggleScaled() {
        if (!isScaling) {
            if (isScaled) {
                scaleDown();
            } else {
                scaleUp();
            }
        }
    }

    public void scaleUp() {
        if (!isScaled) {
            setBackgroundTintList(getResources().getColorStateList(R.color.primary_light));
            scaleUp.start();
        }
    }

    public void scaleDown() {
        if (isScaled) {
            setBackgroundTintList(getResources().getColorStateList(R.color.accent));
            scaleDown.start();
        }
    }

}