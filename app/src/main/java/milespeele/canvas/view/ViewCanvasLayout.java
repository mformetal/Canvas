package milespeele.canvas.view;

import android.content.Context;
import android.os.Handler;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.view.MotionEventCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;

import milespeele.canvas.R;

/**
 * Created by Miles Peele on 7/10/2015.
 */
public class ViewCanvasLayout extends CoordinatorLayout {

    private ViewFabMenu palette;
    private boolean mIsMoving = false;

    private static Handler handler = new Handler();
    private static final int MOVING_DELAY = 750;

    public ViewCanvasLayout(Context context) {
        super(context);
        setSaveEnabled(true);
    }

    public ViewCanvasLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        setSaveEnabled(true);
    }

    public ViewCanvasLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setSaveEnabled(true);
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
        palette = (ViewFabMenu) getChildAt(1); // lol
    }

    private void ifStillMoving() {
        handler.postDelayed(() -> {
            if (mIsMoving) {
                palette.animateOut();
            }
        }, MOVING_DELAY);
    }
}