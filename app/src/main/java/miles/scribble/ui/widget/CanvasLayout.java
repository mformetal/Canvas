package miles.scribble.ui.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Handler;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.util.AttributeSet;
import android.util.Property;
import android.view.MotionEvent;
import android.view.SoundEffectConstants;
import android.view.View;
import android.widget.Toolbar;

import butterknife.BindView;
import butterknife.ButterKnife;
import miles.scribble.R;
import miles.scribble.home.drawing.DrawingCurve;
import miles.scribble.util.ViewUtils;

/**
 * Created by milespeele on 8/7/15.
 */
public class CanvasLayout extends CoordinatorLayout implements DrawingCurve.DrawingCurveListener {

    @BindView(R.id.canvas_surface) CanvasSurface drawer;
    @BindView(R.id.canvas_fab_menu) CircleFabMenu fabMenu;
    @BindView(R.id.canvas_framelayout_animator) RoundedFrameLayout fabFrame;
    @BindView(R.id.canvas_toolbar)  Toolbar toolbar;

    private Rect mRect = new Rect();
    private Paint mShadowPaint;
    private Handler mHandler;
    private float mRadius;

    public CanvasLayout(Context context) {
        super(context);
        init();
    }

    public CanvasLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CanvasLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mHandler = new Handler();

        mShadowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mShadowPaint.setColor(Color.BLACK);
        mShadowPaint.setAlpha(0);
        mShadowPaint.setMaskFilter(new BlurMaskFilter(15, BlurMaskFilter.Blur.OUTER));

        setWillNotDraw(false);
        setClipChildren(false);
        setSaveEnabled(true);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        ButterKnife.bind(this);

        drawer.setListener(this);
    }

    @Override
    protected boolean drawChild(Canvas canvas, View child, long drawingTime) {
        if (mShadowPaint.getAlpha() != 0) {
            if (child == fabMenu) {
                canvas.drawCircle(fabMenu.getCx(),
                        fabMenu.getCy() + (getHeight() - fabMenu.getHeight()),
                        mRadius, mShadowPaint);
            }
        }
        return super.drawChild(canvas, child, drawingTime);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        final float x = ev.getX(), y = ev.getY();

        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (isSystemUISwipe(ev)) {
                    return true;
                }

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
                    if (mShadowPaint.getAlpha() != 0) {
                        drawer.setEnabled(false);
                    }

                    if (!fabMenu.isEnabled()) {
                        fabMenu.setEnabled(true);
                    }
                }
                break;
            case MotionEvent.ACTION_MOVE:
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        ViewUtils.systemUIGone(getRootView());
                    }
                }, 350);
                break;
        }

        return false;
    }

    @Override
    public void toggleFabMenuVisibility(boolean setVisible) {
        if (setVisible) {
            ViewUtils.visible(fabMenu);
        } else {
            ViewUtils.gone(fabMenu);
        }
    }

    private void makeDrawingVisible() {
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                drawer.setEnabled(true);
                undim();
                fabMenu.toggleMenu();
            }
        }, 200);
    }

    void dim() {
        Animator alpha = ObjectAnimator.ofInt(this, ALPHA, 64).setDuration(200);

        Animator radius = ObjectAnimator.ofFloat(this, RADIUS, getHeight()).setDuration(200);

        Animator visibility = ViewUtils.visibleAnimator(toolbar);

        AnimatorSet set = new AnimatorSet();
        set.playTogether(alpha, radius, visibility);
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                drawer.setEnabled(false);
            }
        });
        set.start();
    }

    void undim() {
        Animator alpha = ObjectAnimator.ofInt(this, ALPHA, 0).setDuration(400);

        Animator radius = ObjectAnimator.ofFloat(this, RADIUS, 0).setDuration(400);

        Animator visibility = ViewUtils.goneAnimator(toolbar);

        AnimatorSet set = new AnimatorSet();
        set.playTogether(alpha, radius, visibility);
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                drawer.setEnabled(true);
            }
        });
        set.start();
    }

    public int getBrushColor() {
        return drawer.getBrushColor();
    }

    public int getBackgroundColor() { return drawer.getBackgroundColor(); }

    private Bitmap getDrawerBitmap() {
        return drawer.getDrawingBitmap();
    }

    public Paint getPaint() { return drawer.getCurrentPaint(); }

    private void redo() {
        if (!drawer.redo()) {
            Snackbar.make(this, R.string.snackbar_no_more_redo, Snackbar.LENGTH_SHORT).show();
        }
    }

    public DrawingCurve.State getDrawingCurveState() { return drawer.getState(); }

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

    private void setRadius(float radius) {
        mRadius = radius;
        invalidate();
    }

    private float getRadius() {
        return mRadius;
    }

    private boolean isSystemUISwipe(MotionEvent event) {
        float scrim = getResources().getDimension(R.dimen.system_ui_scrim);

        if (event.getY() <= scrim) {
            return true;
        }

        return event.getY() >= getHeight() - scrim;
    }

    private static final Property<CanvasLayout, Integer> ALPHA = new ViewUtils.IntProperty<CanvasLayout>("alpha") {

        @Override
        public void setValue(CanvasLayout layout, int value) {
            layout.setPaintAlpha(value);
        }

        @Override
        public Integer get(CanvasLayout layout) {
            return layout.getPaintAlpha();
        }
    };

    private final static ViewUtils.FloatProperty<CanvasLayout> RADIUS =
            new ViewUtils.FloatProperty<CanvasLayout>("radius") {
        @Override
        public void setValue(CanvasLayout object, float value) {
            object.setRadius(value);
        }

        @Override
        public Float get(CanvasLayout object) {
            return object.getRadius();
        }
    };
}