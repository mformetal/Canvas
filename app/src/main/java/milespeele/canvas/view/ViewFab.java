package milespeele.canvas.view;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.support.design.widget.FloatingActionButton;
import android.util.AttributeSet;
import android.view.View;

public class ViewFab extends FloatingActionButton {

    private AnimatorSet animateIn;
    private AnimatorSet animateOut;

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
        animateIn = new AnimatorSet();
        animateIn.playTogether(ObjectAnimator.ofFloat(this, "rotation", 0f, 360f),
                ObjectAnimator.ofFloat(this, "alpha", 0, 1));

        animateOut = new AnimatorSet();
        animateOut.playTogether(ObjectAnimator.ofFloat(this, "rotation", 0f, 360f),
                ObjectAnimator.ofFloat(this, "alpha", 1, 0));
    }

    public void show() {
        animateIn.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        animateIn.start();
    }

    public void hide() {
        animateOut.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                setVisibility(View.GONE);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        animateOut.start();
    }

}