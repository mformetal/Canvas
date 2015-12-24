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
import android.view.ViewConfiguration;
import android.widget.FrameLayout;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import milespeele.canvas.R;
import milespeele.canvas.util.AbstractAnimatorListener;
import milespeele.canvas.util.Circle;
import milespeele.canvas.util.Logg;
import milespeele.canvas.util.ViewUtils;

/**
 * Created by milespeele on 8/7/15.
 */
public class ViewCanvasLayout extends CoordinatorLayout implements View.OnClickListener {

    @Bind(R.id.fragment_drawer_canvas) ViewCanvasSurface drawer;
    @Bind(R.id.fragment_drawer_menu) ViewFabMenu menu;
    @Bind(R.id.fragment_drawer_animator) ViewRoundedFrameLayout fabFrame;
    @Bind(R.id.fragment_drawer_button) ViewTypefaceButton button;

    private final Rect hitRect = new Rect();
    private Paint shadowPaint;

    private int[] loc = new int[2];
    private static final int BUTTON_BAR_DURATION = 350;

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

        if (fabFrame.getVisibility() == View.VISIBLE) {
            drawer.setOnTouchListener(null);
            fabFrame.getHitRect(hitRect);
            if (!hitRect.contains((int) x, (int) y)) {
                if (getContext() instanceof Activity) {
                    ((Activity) getContext()).onBackPressed();
                }
            }
            menu.setEnabled(false);
            return false;
        }

        if (menu.isVisible()) {
            if (menuContainsTouch(ev)) {
                return true;
            }
        } else {
            if (Circle.contains(menu.getCenterX() - ev.getX(),
                    menu.getCenterY() - (ev.getY() - (getHeight() - menu.getHeight())),
                    ViewUtils.radius(menu.toggle))) {
                drawer.setOnTouchListener(null);
                return true;
            }
        }

        drawer.setOnTouchListener(drawer);

        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (menuContainsTouch(ev)) {
            ev.offsetLocation(0, -(getHeight() - menu.getHeight()));
            menu.onTouchEvent(ev);
        }

        return true;
    }

    @Override
    @OnClick(R.id.fragment_drawer_button)
    public void onClick(View v) {
        drawer.onButtonClicked();
    }

    private boolean menuContainsTouch(MotionEvent event) {
        return Circle.contains(menu.getCenterX() - event.getX(),
                (menu.getCenterY() + (getHeight() - menu.getHeight())) - event.getY(),
                menu.getCircleRadius());
    }

    public void setButtonGone() {
        ObjectAnimator gone = ObjectAnimator.ofFloat(button, View.ALPHA, 1f, 0f);
        gone.setDuration(BUTTON_BAR_DURATION);
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

        ObjectAnimator visibility = ObjectAnimator.ofFloat(button, View.ALPHA, 0f, 1f);
        visibility.setDuration(BUTTON_BAR_DURATION);
        visibility.addListener(new AbstractAnimatorListener() {

            @Override
            public void onAnimationStart(Animator animation) {
                button.setVisibility(View.VISIBLE);
            }
        });
        visibility.start();
    }

    public void setMenuListener(ViewFabMenu.ViewFabMenuListener other) {
        menu.setListener(other);
    }

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
        menu.fadeOut();
        drawer.ink();

        if (button.getVisibility() == View.VISIBLE) {
            setButtonGone();
        }
    }

    public void erase() {
        drawer.erase();
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