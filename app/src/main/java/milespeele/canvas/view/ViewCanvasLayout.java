package milespeele.canvas.view;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.view.MotionEventCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.lang.ref.WeakReference;

import butterknife.Bind;
import butterknife.ButterKnife;
import milespeele.canvas.R;
import milespeele.canvas.util.AbstractAnimatorListener;

/**
 * Created by milespeele on 8/7/15.
 */
public class ViewCanvasLayout extends CoordinatorLayout {

    @Bind(R.id.fragment_drawer_canvas) ViewCanvasSurface drawer;
    @Bind(R.id.fragment_drawer_menu) ViewFabMenu menu;

    private boolean mIsMoving = false;
    private static final int MOVING_DELAY = 750;
    private final MyHandler handler = new MyHandler(this);
    private final static class MyHandler extends Handler {
        private final WeakReference<ViewCanvasLayout> ref;

        public MyHandler(ViewCanvasLayout view) {
            ref = new WeakReference<>(view);
        }

        @Override
        public void handleMessage(Message msg) {
            if (ref.get() != null) {
                ref.get().ifStillMoving();
            }
        }
    }

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

        menu.setVisibility(View.VISIBLE);

        ObjectAnimator.ofFloat(menu, "alpha", 0f, 1f)
                .setDuration(1000)
                .start();
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

    public void unreveal() {
        drawer.getThread().unreveal();

        ObjectAnimator.ofFloat(menu, "alpha", 1f, 0f)
                .setDuration(1000)
                .start();
    }

    public float getBrushWidth() {
        return drawer.getBrushWidth();
    }

    public int getBrushColor() {
        return drawer.getBrushColor();
    }

    public Bitmap getDrawerBitmap() {
        return drawer.getDrawingBitmap();
    }
}
