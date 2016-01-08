package milespeele.canvas.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;

/**
 * Created by mbpeele on 1/8/16.
 */
public class ViewNavDrawer extends ViewGroup {

    private boolean isAnimating = false;

    public ViewNavDrawer(Context context) {
        super(context);
        init();
    }

    public ViewNavDrawer(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ViewNavDrawer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public ViewNavDrawer(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {

    }

    public void toggle() {
        ViewCanvasLayout parent = (ViewCanvasLayout) getParent();
        if (!isAnimating) {
            if (getVisibility() == View.GONE) {
                parent.bringChildToFront(this);
                int right = getRight();
                setTranslationX(-right);

                ObjectAnimator alpha = ObjectAnimator
                        .ofInt(parent, ViewCanvasLayout.ALPHA, 128).setDuration(350);

                ObjectAnimator visible = ObjectAnimator.ofFloat(this, View.TRANSLATION_X, 0);
                visible.setDuration(350);
                visible.setInterpolator(new DecelerateInterpolator());
                visible.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        setVisibility(View.VISIBLE);
                        alpha.start();
                        isAnimating = true;
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        isAnimating = false;
                    }
                });
                visible.start();
            } else {
                int right = getRight();

                ObjectAnimator alpha = ObjectAnimator.ofInt(parent, ViewCanvasLayout.ALPHA, 0).setDuration(350);

                ObjectAnimator gone = ObjectAnimator.ofFloat(this, View.TRANSLATION_X, -right);
                gone.setDuration(350);
                gone.setInterpolator(new AccelerateInterpolator());
                gone.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        alpha.start();
                        isAnimating = true;
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        isAnimating = false;
                        setVisibility(View.GONE);
                    }
                });
                gone.start();
            }
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {

    }
}
