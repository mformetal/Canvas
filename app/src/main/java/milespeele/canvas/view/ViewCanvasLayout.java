package milespeele.canvas.view;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Shader;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.view.MotionEventCompat;
import android.util.AttributeSet;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.animation.AccelerateDecelerateInterpolator;

import java.lang.ref.WeakReference;

import butterknife.Bind;
import butterknife.ButterKnife;
import milespeele.canvas.R;
import milespeele.canvas.util.AbstractAnimatorListener;
import milespeele.canvas.util.Logg;

/**
 * Created by milespeele on 8/7/15.
 */
public class ViewCanvasLayout extends CoordinatorLayout {

    @Bind(R.id.fragment_drawer_canvas) ViewCanvasSurface drawer;
    @Bind(R.id.fragment_drawer_menu) ViewFabMenu menu;

    private float circle = 0;
    private static final int DURATION = 1000;
    private float centerX, centerY;
    private Paint revealPaint;
    private Path revealPath;
    private ObjectAnimator animator;

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
        revealPath = new Path();

        animator = new ObjectAnimator();

        revealPaint = new Paint();
        revealPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));

        setWillNotDraw(false);
        setClipChildren(false);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        ButterKnife.bind(this);
        drawer.requestFocus();
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawCircle(centerX, centerY, circle, revealPaint);
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

    public Animator reveal(float cx, float cy) {
        Display display = ((Activity) getContext()).getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);

        centerX = (cx == 0) ? size.x / 2 : cx;
        centerY = (cy == 0) ? size.y / 2 : cy;

        animator = ObjectAnimator.ofFloat(this, "circle", size.y);
        animator.setDuration(DURATION);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        return animator;
    }

    public Animator unreveal() {
        animator = ObjectAnimator.ofFloat(this, "circle", 0);
        animator.setDuration(DURATION);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        return animator;
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

    public float getCircle() {
        return circle;
    }

    public void setCircle(float circle) {
        this.circle = circle;
        invalidate();
    }

    @Override
    protected boolean drawChild(Canvas canvas, View child, long drawingTime) {
        if (animator.isRunning()) {
            final int state = canvas.save();

            revealPath.reset();
            revealPath.addCircle(centerX, centerY, circle, Path.Direction.CW);

            canvas.clipPath(revealPath);

            boolean isInvalided = super.drawChild(canvas, child, drawingTime);

            canvas.restoreToCount(state);

            return isInvalided;
        }

        return super.drawChild(canvas, child, drawingTime);
    }
}
