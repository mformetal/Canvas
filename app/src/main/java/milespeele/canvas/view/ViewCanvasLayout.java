package milespeele.canvas.view;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
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
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.app.AlertDialog;
import android.transition.ArcMotion;
import android.transition.TransitionManager;
import android.util.AttributeSet;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.FrameLayout;

import java.lang.ref.WeakReference;

import butterknife.Bind;
import butterknife.ButterKnife;
import milespeele.canvas.R;
import milespeele.canvas.transition.TransitionFabToDialog;
import milespeele.canvas.util.Logg;
import milespeele.canvas.util.ViewUtils;

/**
 * Created by milespeele on 8/7/15.
 */
public class ViewCanvasLayout extends CoordinatorLayout implements ViewFabMenu.ViewFabMenuListener {

    @Bind(R.id.fragment_drawer_canvas) ViewCanvasSurface drawer;
    @Bind(R.id.fragment_drawer_menu) ViewFabMenu menu;
    @Bind(R.id.fragment_drawer_animator) FrameLayout fabFrame;

    private ViewFabMenu.ViewFabMenuListener listener;
    private float circle = 0;
    private static final int DURATION = 750;
    private float centerX, centerY;
    private Paint revealPaint;
    private Path revealPath;
    private ObjectAnimator animator;
    private int height, width;

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
        menu.setListener(this);
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

    @Override
    public void onFabMenuButtonClicked(ViewFab view) {
        if (listener != null) {
            if (view.getId() == R.id.menu_new_canvas) {
                AlertDialog alert = new AlertDialog.Builder(getContext())
                        .setTitle(getResources().getString(R.string.alert_dialog_new_canvas_title))
                        .setMessage(getResources().getString(R.string.alert_dialog_new_canvas_body))
                        .setPositiveButton(getResources().getString(R.string.alert_dialog_new_canvas_pos_button),
                                (dialog, which) -> {
                                    animateFabExpansion(view);
                                })
                        .setNegativeButton(getResources().getString(R.string.fragment_color_picker_nah),
                                (dialog, which) -> {
                                    dialog.dismiss();
                                })
                        .create();
                alert.show();
            } else {
                animateFabExpansion(view);
            }
        }
    }

    private void animateFabExpansion(final ViewFab view) {
        listener.onFabMenuButtonClicked(view);

        fabFrame.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (fabFrame.getWidth() > 0) {
                    fabFrame.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                    int endColor = getResources().getColor(R.color.primary_dark);
                    if (view.getId() == R.id.menu_stroke_color || view.getId() == R.id.menu_new_canvas) {
                        endColor = Color.TRANSPARENT;
                    }
                    TransitionFabToDialog transitionFabToDialog = new TransitionFabToDialog(endColor);
                    transitionFabToDialog.addTarget(view);
                    transitionFabToDialog.addTarget(fabFrame);
                    TransitionManager.beginDelayedTransition(ViewCanvasLayout.this, transitionFabToDialog);
                }
            }
        });
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

    public void ink() {
        drawer.ink();
    }

    public float getCircle() {
        return circle;
    }

    public void setCircle(float circle) {
        this.circle = circle;
        invalidate();
    }
}