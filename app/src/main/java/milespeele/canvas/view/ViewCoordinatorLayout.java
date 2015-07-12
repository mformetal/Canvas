package milespeele.canvas.view;

import android.content.Context;
import android.os.Handler;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPropertyAnimatorListener;
import android.support.v4.view.animation.LinearOutSlowInInterpolator;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.BounceInterpolator;
import android.view.animation.Interpolator;
import android.widget.LinearLayout;

import milespeele.canvas.R;
import milespeele.canvas.util.Logger;

/**
 * Created by Miles Peele on 7/10/2015.
 */
public class ViewCoordinatorLayout extends CoordinatorLayout {

    private ViewFabMenu palette;
    private boolean mIsMoving = false;

    private static Handler handler = new Handler();
    private static final int MOVING_DELAY = 750;

    public ViewCoordinatorLayout(Context context) {
        super(context);
    }

    public ViewCoordinatorLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ViewCoordinatorLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean onInterceptTouchEvent (MotionEvent ev) {
        final int action = MotionEventCompat.getActionMasked(ev);
        switch (action) {
            case MotionEvent.ACTION_MOVE:
                mIsMoving = true;
                ifStillMoving();
                break;
            case MotionEvent.ACTION_UP:
                mIsMoving = false;
                break;
            case MotionEvent.ACTION_CANCEL:
                mIsMoving = false;
                break;
        }
        return false;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        palette = (ViewFabMenu) getChildAt(1);
    }

    private void ifStillMoving() {
        handler.postDelayed(() -> {
            if (mIsMoving) {
                palette.animateOut();
            }
        }, MOVING_DELAY);
    }
}
