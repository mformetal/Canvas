package milespeele.canvas.view;

import android.content.Context;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.animation.BounceInterpolator;
import android.view.animation.Interpolator;

/**
 * Created by milespeele on 7/8/15.
 */
public class ViewFabDrawerBehavior extends FloatingActionButton.Behavior {

    private boolean mIsAnimatingOut;

    private final static Interpolator INTERPOLATOR = new BounceInterpolator();

    public ViewFabDrawerBehavior(Context context, AttributeSet attributeSet) {
        super();
    }

    @Override
    public boolean onInterceptTouchEvent (CoordinatorLayout parent, FloatingActionButton child, MotionEvent ev) {
        final int action = MotionEventCompat.getActionMasked(ev);
        switch (action) {
            case MotionEvent.ACTION_MOVE:
                animateOut(child);
            case MotionEvent.ACTION_UP:
                animateIn(child);
        }
        return false;
    }

    private void animateOut(final FloatingActionButton button) {
        ViewCompat.animate(button).scaleX(0).scaleY(0).alpha(0)
                .setInterpolator(INTERPOLATOR)
                .withLayer()
                .start();
//        ViewCompat.animate(button).scaleX(0.0F).scaleY(0.0F).alpha(0.0F).setInterpolator(
//                new AccelerateDecelerateInterpolator()).withLayer()
//                .setListener(new ViewPropertyAnimatorListener() {
//                    public void onAnimationStart(View view) {
//                        ViewFabDrawerBehavior.this.mIsAnimatingOut = true;
//                    }
//
//                    public void onAnimationCancel(View view) {
//                        ViewFabDrawerBehavior.this.mIsAnimatingOut = false;
//                    }
//
//                    public void onAnimationEnd(View view) {
//                        ViewFabDrawerBehavior.this.mIsAnimatingOut = false;
//                        view.setVisibility(View.GONE);
//                    }
//                }).start();
    }

    private void animateIn(FloatingActionButton button) {
//        button.setVisibility(View.VISIBLE);
//        ViewCompat.animate(button).scaleX(1.0F).scaleY(1.0F).alpha(1.0F)
//                .setInterpolator(new AccelerateDecelerateInterpolator())
//                .withLayer().setListener(null)
//                .start();
//        ViewCompat.animate(button).scaleX(1).scaleY(1).alpha(1)
//                .setInterpolator(INTERPOLATOR)
//                .withLayer()
//                .start();
    }

}
