package milespeele.canvas.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.view.MotionEventCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;

import butterknife.Bind;
import butterknife.ButterKnife;
import milespeele.canvas.R;

/**
 * Created by milespeele on 8/7/15.
 */
public class ViewCanvasLayout extends CoordinatorLayout {

    @Bind(R.id.fragment_drawer_canvas) ViewCanvas drawer;
    @Bind(R.id.fragment_drawer_menu) ViewFabMenu menu;

    private boolean mIsMoving = false;
    private static Handler handler = new Handler();
    private static final int MOVING_DELAY = 750;

    public ViewCanvasLayout(Context context) {
        super(context);
        init();
    }

    public ViewCanvasLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ViewCanvasLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setClipChildren(false);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        ButterKnife.bind(this);
        drawer.requestFocus();
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

    private void ifStillMoving() {
        handler.postDelayed(() -> {
            if (mIsMoving) {
                menu.setVisibilityGone();
                handler.removeCallbacksAndMessages(null);
            }
        }, MOVING_DELAY);
    }

    public float getBrushWidth() {
        return drawer.getBrushWidth();
    }

    public int getPaintAlpha() { return drawer.getPaintAlpha(); }

    public Bitmap getDrawerBitmap() { return drawer.getBitmap(); }

    public int getDrawerColor() { return drawer.getCurrentStrokeColor(); }
}
