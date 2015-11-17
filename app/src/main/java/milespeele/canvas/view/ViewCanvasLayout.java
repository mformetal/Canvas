package milespeele.canvas.view;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.view.MotionEventCompat;
import android.text.Layout;
import android.util.AttributeSet;
import android.util.Property;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.Toast;

import java.lang.ref.WeakReference;

import butterknife.Bind;
import butterknife.ButterKnife;
import milespeele.canvas.R;
import milespeele.canvas.util.Logg;
import milespeele.canvas.util.ViewUtils;

/**
 * Created by milespeele on 8/7/15.
 */
public class ViewCanvasLayout extends CoordinatorLayout implements ViewFabMenu.ViewFabMenuListener {

    @Bind(R.id.fragment_drawer_canvas) ViewCanvasSurface drawer;
    @Bind(R.id.fragment_drawer_menu) ViewFabMenu menu;
    @Bind(R.id.fragment_drawer_animator) FrameLayout fabFrame;

    private float circle = 0;
    private static final int DURATION = 750;
    private float centerX, centerY;
    private Paint revealPaint, shadowPaint;
    private Path revealPath;
    private ObjectAnimator animator;
    private int height, width;
    private Rect hitRect;
    private int[] loc = new int[2];

    private ViewFabMenu.ViewFabMenuListener listener;

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
        hitRect = new Rect();

        revealPath = new Path();

        animator = new ObjectAnimator();

        revealPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        revealPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));

        shadowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        shadowPaint.setColor(Color.BLACK);
        shadowPaint.setAlpha(0);

        setWillNotDraw(false);
        setDrawingCacheEnabled(true);
        setClipChildren(false);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        ButterKnife.bind(this);
        drawer.requestFocus();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        height = h;
        width = w;
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawCircle(centerX, centerY, circle, revealPaint);
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        if (shadowPaint.getAlpha() != 0) {
            fabFrame.getLocationOnScreen(loc);

            CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) fabFrame.getLayoutParams();

            float scaleWidth = fabFrame.getScaleX() * fabFrame.getWidth();
            float scaleHeight = fabFrame.getScaleY() * fabFrame.getHeight();

            float left = loc[0];
            float top = loc[1] - params.topMargin * .8f;
            float right = left + scaleWidth;
            float bottom = top + scaleHeight;

            canvas.drawRect(left, 0, right, top, shadowPaint);
            canvas.drawRect(0, 0, left, canvas.getHeight(), shadowPaint);
            canvas.drawRect(right, 0, canvas.getWidth(), canvas.getHeight(), shadowPaint);
            canvas.drawRect(left, bottom, right, canvas.getHeight(), shadowPaint);
        }
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

    @Override
    public void onFabMenuButtonClicked(final ViewFab v) {
        listener.onFabMenuButtonClicked(v);
    }

    @Override
    public boolean onInterceptTouchEvent (MotionEvent ev) {
        final float x = ev.getX(), y = ev.getY();
        if (fabFrame.getVisibility() == View.VISIBLE) {
            fabFrame.getHitRect(hitRect);
            if (!hitRect.contains((int) x, (int) y)) {
                if (getContext() instanceof Activity) {
                    ((Activity) getContext()).onBackPressed();
                    return true;
                }
            }
        }

        final int action = MotionEventCompat.getActionMasked(ev);
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                break;

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
                menu.fadeOut();
                handler.removeCallbacksAndMessages(null);
            }
        }, MOVING_DELAY);
    }

    public Animator reveal(float cx, float cy) {
        if (width == 0 || height == 0) {
            Display display = ((Activity) getContext()).getWindowManager().getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);

            height = size.y;
            width = size.x;
        }

        centerX = (cx == 0) ? width / 2 : cx;
        centerY = (cy == 0) ? height / 2 : cy;

        animator = ObjectAnimator.ofFloat(this, "circle", height);
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

    public void setMenuListener(ViewFabMenu.ViewFabMenuListener other) {
        listener = other;
        menu.setListener(this);
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

    public void redo() {
        drawer.redo();
    }

    public void undo() {
        drawer.undo();
    }

    public void erase() {
        drawer.erase();
    }

    public float getCircle() {
        return circle;
    }

    public void setCircle(float circle) {
        this.circle = circle;
        invalidate();
    }

    public void setPaintAlpha(int alpha) {
        shadowPaint.setAlpha(alpha);
        invalidate();
    }

    public int getPaintAlpha() {
        return shadowPaint.getAlpha();
    }

    public static final Property<ViewCanvasLayout, Integer> ALPHA = new ViewUtils.IntProperty<ViewCanvasLayout>("alpha") {

        @Override
        public void setValue(ViewCanvasLayout layout, int value) {
            layout.setPaintAlpha(value);
        }

        @Override
        public Integer get(ViewCanvasLayout layout) {
            return layout.getPaintAlpha();
        }
    };
}