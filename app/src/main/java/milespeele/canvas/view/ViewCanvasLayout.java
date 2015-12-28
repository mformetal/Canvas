package milespeele.canvas.view;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.util.AttributeSet;
import android.util.Property;
import android.view.MotionEvent;
import android.view.SoundEffectConstants;
import android.view.View;

import butterknife.Bind;
import butterknife.ButterKnife;
import milespeele.canvas.R;
import milespeele.canvas.drawing.DrawingCurve;
import milespeele.canvas.util.Circle;
import milespeele.canvas.util.Logg;
import milespeele.canvas.util.ViewUtils;

/**
 * Created by milespeele on 8/7/15.
 */
public class ViewCanvasLayout extends CoordinatorLayout implements
        ViewFabMenu.ViewFabMenuListener, DrawingCurve.DrawingCurveListener, ViewOptionsMenu.ViewOptionsMenuListener {

    @Bind(R.id.fragment_drawer_canvas) ViewCanvasSurface drawer;
    @Bind(R.id.fragment_drawer_menu) ViewFabMenu fabMenu;
    @Bind(R.id.fragment_drawer_animator) ViewRoundedFrameLayout fabFrame;
    @Bind(R.id.fragment_drawer_options_menu) ViewOptionsMenu optionsMenu;

    private final Rect hitRect = new Rect();
    private Paint shadowPaint;

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

        fabMenu.addListener(this);
        drawer.setListener(this);
        optionsMenu.addListener(this);
    }

    @Override
    protected boolean drawChild(Canvas canvas, View child, long drawingTime) {
        if (shadowPaint.getAlpha() != 0) {
            canvas.save();
            canvas.drawPaint(shadowPaint);
            canvas.restore();

            int alpha = shadowPaint.getAlpha();
            if (child == fabMenu) {
                for (int x = 0; x < fabMenu.getChildCount(); x++) {
                    fabMenu.getChildAt(x).setAlpha((ViewUtils.MAX_ALPHA - alpha) / ViewUtils.MAX_ALPHA);
                }
            }
        }
        return super.drawChild(canvas, child, drawingTime);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        final float x = ev.getX(), y = ev.getY();

        if (fabFrame.getVisibility() == View.VISIBLE &&
                !fabFrame.isAnimating()) {
            drawer.setOnTouchListener(null);
            fabFrame.getHitRect(hitRect);
            if (!hitRect.contains((int) x, (int) y)) {
                if (getContext() instanceof Activity) {
                    playSoundEffect(SoundEffectConstants.CLICK);
                    ((Activity) getContext()).onBackPressed();
                }
            }
            fabMenu.setEnabled(false);
            return false;
        }

        fabMenu.setEnabled(true);

        if (fabMenu.isVisible()) {
            if (menuContainsTouch(ev)) {
                drawer.setEnabled(false);
                return false;
            }
        }

        drawer.setEnabled(true);

        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (menuContainsTouch(ev)) {
            ev.offsetLocation(0, -(getHeight() - fabMenu.getHeight()));
            fabMenu.onTouchEvent(ev);
        }

        return false;
    }

    @Override
    public void onFabMenuButtonClicked(ViewFab v) {
        v.performClick();

        switch (v.getId()) {
            case R.id.menu_toggle:
                fabMenu.toggleMenu();
                break;
            case R.id.menu_undo:
                undo();
                break;
            case R.id.menu_redo:
                redo();
                break;
            case R.id.menu_erase:
                erase();
                fabMenu.eraser.toggleScaled();
                break;
            case R.id.menu_ink:
                ink();
                break;
        }

        if (v.getId() != R.id.menu_toggle) {
            if (v.getId() != R.id.menu_erase && fabMenu.eraser.isScaledUp()) {
                fabMenu.eraser.scaleDown();
            }
        }
    }

    @Override
    public void onDrawingCurveOptionsMenuVisibilityRequest(boolean setVisible) {
        if (setVisible) {
            if (optionsMenu.getVisibility() == View.GONE) {
                ViewUtils.visible(optionsMenu, 350);
            } else {
                ObjectAnimator.ofFloat(optionsMenu, View.TRANSLATION_Y,
                        optionsMenu.getTranslationY() - optionsMenu.getHeight())
                        .setDuration(350)
                        .start();
            }
        } else {
            ObjectAnimator.ofFloat(optionsMenu, View.TRANSLATION_Y,
                    optionsMenu.getTranslationY() + optionsMenu.getHeight())
                    .setDuration(350)
                    .start();
        }
    }

    @Override
    public void onDrawingCurveFabMenuVisibilityRequest(boolean setVisible) {
        if (setVisible) {
            ViewUtils.visible(fabMenu, 350);
        } else {
            ViewUtils.gone(fabMenu, 350);
        }
    }

    @Override
    public void onDrawingCurveSnbackRequest(int stringId, int length) {
        Snackbar.make(this, stringId, length).show();
    }

    @Override
    public void onOptionsMenuCancel() {
        drawer.onOptionsMenuCancel();
    }

    @Override
    public void onOptionsMenuButtonClicked(View view) {
    }

    @Override
    public void onOptionsMenuAccept() {
        drawer.onOptionsMenuAccept();
    }

    private boolean menuContainsTouch(MotionEvent event) {
        return Circle.contains(fabMenu.getCenterX() - event.getX(),
                (fabMenu.getCenterY() + (getHeight() - fabMenu.getHeight())) - event.getY(),
                fabMenu.getCircleRadius());
    }

    public void setMenuListeners(ViewFabMenu.ViewFabMenuListener viewFabMenuListener) {
        fabMenu.addListener(viewFabMenuListener);
    }

    public int getBrushColor() {
        return drawer.getBrushColor();
    }

    public Bitmap getDrawerBitmap() {
        return drawer.getDrawingBitmap();
    }

    public Paint getPaint() { return drawer.getCurrentPaint(); }

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