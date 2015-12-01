package milespeele.canvas.view;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.view.MotionEventCompat;
import android.util.AttributeSet;
import android.util.Property;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

import java.lang.ref.WeakReference;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import milespeele.canvas.R;
import milespeele.canvas.util.AbstractAnimatorListener;
import milespeele.canvas.util.ViewUtils;

/**
 * Created by milespeele on 8/7/15.
 */
public class ViewCanvasLayout extends CoordinatorLayout implements View.OnClickListener {

    @Bind(R.id.fragment_drawer_canvas) ViewCanvasSurface drawer;
    @Bind(R.id.fragment_drawer_menu) ViewFabMenu menu;
    @Bind(R.id.fragment_drawer_animator) FrameLayout fabFrame;
    @Bind(R.id.fragment_drawer_button) ViewTypefaceButton button;

    private final Rect hitRect = new Rect();
    private Paint shadowPaint;

    private float circle = 0;
    private int[] loc = new int[2];

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
        shadowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        shadowPaint.setColor(Color.BLACK);
        shadowPaint.setAlpha(0);

        setWillNotDraw(false);
        setClipChildren(false);
        setSaveEnabled(true);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        ButterKnife.bind(this);
        drawer.requestFocus();
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
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        final float x = ev.getX(), y = ev.getY();
        final int action = MotionEventCompat.getActionMasked(ev);

        if (fabFrame.getVisibility() == View.VISIBLE) {
            fabFrame.getHitRect(hitRect);
            if (!hitRect.contains((int) x, (int) y)) {
                if (getContext() instanceof Activity) {
                    ((Activity) getContext()).onBackPressed();
                    return true;
                }
            }
        }

        if (menu.isVisible()) {
            if (y >= getHeight() - menu.getHeight()) {
                float centerX = ViewUtils.centerX(menu);
                float centerY = getHeight();
                double rad = Math.pow(menu.getCircleRadius(), 2);
                double pyth = Math.pow(x - centerX, 2) + Math.pow(y - centerY, 2);
                if (pyth <= rad) {
                    drawer.setOnTouchListener(null);
                    return false;
                }
            }
        }

        drawer.setOnTouchListener(drawer);

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                break;

            case MotionEvent.ACTION_MOVE:
                if (fabFrame.getVisibility() == View.VISIBLE) {
                    fabFrame.getHitRect(hitRect);
                    if (!hitRect.contains((int) x, (int) y)) {
                        mIsMoving = true;
                        ifStillMoving();
                    }
                } else {
                    mIsMoving = true;
                    ifStillMoving();
                }
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                mIsMoving = false;
                break;
        }
        return false;
    }

    @Override
    @OnClick(R.id.fragment_drawer_button)
    public void onClick(View v) {
        drawer.onButtonClicked();
    }

    public void setButtonGone() {
        ObjectAnimator gone = ObjectAnimator.ofFloat(button, ViewUtils.ALPHA, 1f, 0f);
        gone.setDuration(350);
        gone.addListener(new AbstractAnimatorListener() {

            @Override
            public void onAnimationEnd(Animator animation) {
                button.setVisibility(View.GONE);
            }
        });
        gone.start();
    }

    public void setButtonVisible(String text) {
        button.setText(text);

        ObjectAnimator visibility = ObjectAnimator.ofFloat(button, ViewUtils.ALPHA, 0f, 1f);
        visibility.setDuration(350);
        visibility.addListener(new AbstractAnimatorListener() {

            @Override
            public void onAnimationStart(Animator animation) {
                button.setVisibility(View.VISIBLE);
            }
        });
        visibility.start();
    }

    private void ifStillMoving() {
        handler.postDelayed(() -> {
            if (mIsMoving) {
                menu.fadeOut();
                handler.removeCallbacksAndMessages(null);
            }
        }, MOVING_DELAY);
    }

    public void setMenuListener(ViewFabMenu.ViewFabMenuListener other) {
        menu.setListener(other);
    }

    public Paint getCurrentPaint() { return drawer.getCurrentPaint(); }

    public int getBrushColor() {
        return drawer.getBrushColor();
    }

    public Bitmap getDrawerBitmap() {
        return drawer.getDrawingBitmap();
    }

    public void redo() {
        if (!drawer.redo()) {
            Snackbar.make(this, R.string.snackbar_no_more_redo, Snackbar.LENGTH_SHORT).show();
        }
    }

    public void undo() {
        if (!drawer.undo()) {
            Snackbar.make(this, R.string.snackbar_no_more_undo, Snackbar.LENGTH_SHORT).show();
        }
    }

    public void ink() {
        drawer.ink();
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

    public static final Property<ViewCanvasLayout, Float> CIRCLE = new ViewUtils.FloatProperty<ViewCanvasLayout>("circle") {

        @Override
        public Float get(ViewCanvasLayout object) {
            return object.getCircle();
        }

        @Override
        public void setValue(ViewCanvasLayout object, float value) {
            object.setCircle(value);
        }
    };
}