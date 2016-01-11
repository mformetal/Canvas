package milespeele.canvas.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.Region;
import android.os.Handler;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.util.AttributeSet;
import android.util.Property;
import android.view.MotionEvent;
import android.view.SoundEffectConstants;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;

import butterknife.Bind;
import butterknife.ButterKnife;
import milespeele.canvas.R;
import milespeele.canvas.drawing.DrawingCurve;
import milespeele.canvas.fragment.FragmentDrawer;
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
    @Bind(R.id.fragment_drawer_save_animation) ViewSaveAnimator saveAnimator;

    private final Rect mRect = new Rect();
    private final Path mPath = new Path();
    private Paint mShadowPaint;
    private float mStartX, mStartY;
    private long mStartTime;
    private Handler mHandler;

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
        mHandler = new Handler();

        mShadowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mShadowPaint.setColor(Color.BLACK);
        mShadowPaint.setAlpha(0);

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
        if (mShadowPaint.getAlpha() != 0) {
            canvas.drawPaint(mShadowPaint);

            int paintAlpha = mShadowPaint.getAlpha();
            float viewAlpha = paintAlpha / 255f;
            fabMenu.setAlpha(1f - viewAlpha);
            optionsMenu.setAlpha(1f - viewAlpha);
        }
        return super.drawChild(canvas, child, drawingTime);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        final float x = ev.getX(), y = ev.getY();

        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mStartX = x;
                mStartY = y;
                mStartTime = ev.getEventTime();

                if (fabFrame.getVisibility() == View.VISIBLE && !fabFrame.isAnimating()) {
                    drawer.setEnabled(false);
                    fabMenu.setEnabled(false);
                    fabFrame.getHitRect(mRect);

                    if (!mRect.contains((int) x, (int) y)) {
                        if (getContext() instanceof Activity) {
                            playSoundEffect(SoundEffectConstants.CLICK);
                            ((Activity) getContext()).onBackPressed();
                        }
                    }
                    return false;
                } else {
                    drawer.setEnabled(true);
                    fabMenu.setEnabled(true);
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (detectSwipe(ev)) {
                    drawer.setEnabled(false);
                }
                break;
        }

        if (ev.getAction() == MotionEvent.ACTION_MOVE) {
            mHandler.postDelayed(() -> ViewUtils.systemUIGone(getRootView()), 350);
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
            case R.id.menu_navigation:
                mHandler.removeCallbacksAndMessages(null);
                break;
        }

        if (v.getId() != R.id.menu_toggle) {
            if (v.getId() != R.id.menu_erase && fabMenu.eraser.isScaledUp()) {
                fabMenu.eraser.scaleDown();
            }
        }
    }

    @Override
    public void toggleOptionsMenuVisibilty(boolean setVisible, DrawingCurve.State state) {
        if (setVisible) {
            optionsMenu.setState(state);
        } else {
            ViewUtils.gone(optionsMenu);
        }
    }

    @Override
    public void toggleFabMenuVisibility(boolean setVisible) {
        if (setVisible) {
            ViewUtils.visible(fabMenu);
        } else {
            ViewUtils.gone(fabMenu);
        }
    }

    @Override
    public void onOptionsMenuCancel() {
        drawer.onOptionsMenuCancel();
    }

    @Override
    public void onOptionsMenuButtonClicked(View view, DrawingCurve.State state) {}

    @Override
    public void onOptionsMenuAccept() {
        drawer.onOptionsMenuAccept();
    }

    public void startSaveBitmapAnimation() {
        saveAnimator.setColors(drawer.getBackgroundColor());
        saveAnimator.setTranslationY(getHeight());

        AnimatorSet animatorSet = new AnimatorSet();

        Animator alpha = ObjectAnimator.ofInt(this, ViewCanvasLayout.ALPHA, 128);
        alpha.setInterpolator(new LinearInterpolator());
        alpha.setDuration(500);

        ObjectAnimator yPosition = ObjectAnimator.ofFloat(saveAnimator, View.TRANSLATION_Y, 0);
        yPosition.setDuration(500);
        yPosition.setInterpolator(new DecelerateInterpolator());
        yPosition.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                saveAnimator.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                saveAnimator.startAnimation();
            }
        });

        animatorSet.playTogether(alpha, yPosition);
        animatorSet.start();
    }

    public void stopSaveAnimation(AnimatorListenerAdapter adapter) {
        saveAnimator.stopAnimation(adapter);
    }

    public void setMenuListeners(FragmentDrawer fragmentDrawer) {
        fabMenu.addListener(fragmentDrawer);
        optionsMenu.addListener(fragmentDrawer);
    }

    public int getBrushColor() {
        return drawer.getBrushColor();
    }

    public int getBackgroundColor() { return drawer.getBackgroundColor(); }

    public Bitmap getDrawerBitmap() {
        return drawer.getDrawingBitmap();
    }

    public Paint getPaint() { return drawer.getCurrentPaint(); }

    private void redo() {
        if (!drawer.redo()) {
            Snackbar.make(this, R.string.snackbar_no_more_redo, Snackbar.LENGTH_SHORT).show();
        }
    }

    private void undo() {
        if (!drawer.undo()) {
            Snackbar.make(this, R.string.snackbar_no_more_undo, Snackbar.LENGTH_SHORT).show();
        }
    }

    private void ink() {
        drawer.ink();
    }

    private void erase() {
        drawer.erase();
    }

    private void setPaintAlpha(int alpha) {
        mShadowPaint.setAlpha(alpha);
        invalidate();
    }

    private int getPaintAlpha() {
        return mShadowPaint.getAlpha();
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

    private boolean detectSwipe(MotionEvent event) {
        float swipeThreshold = ViewUtils.dpToPx(getResources().getDimension(R.dimen.status_bar_height), getContext());
        final long elapsed = event.getEventTime() - mStartTime;

        if (mStartY <= swipeThreshold
                && event.getY() > mStartY + swipeThreshold
                && elapsed < 500) {
            return true;
        }

        if (mStartY >= getHeight() - swipeThreshold
                && event.getY() < mStartY - swipeThreshold
                && elapsed < 500) {
            return true;
        }

        return false;
    }
}