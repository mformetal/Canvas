package miles.canvas.ui.widget;

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
import android.graphics.Path;
import android.graphics.Rect;
import android.os.Handler;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.util.AttributeSet;
import android.util.Property;
import android.view.MotionEvent;
import android.view.SoundEffectConstants;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.Window;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.LinearLayout;

import java.lang.reflect.Type;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;
import miles.canvas.MainApp;
import miles.canvas.R;
import miles.canvas.data.event.EventBitmapChosen;
import miles.canvas.data.event.EventClearCanvas;
import miles.canvas.data.event.EventColorChosen;
import miles.canvas.data.event.EventFilenameChosen;
import miles.canvas.data.event.EventTextChosen;
import miles.canvas.ui.activity.HomeActivity;
import miles.canvas.ui.activity.HomeActivity;
import miles.canvas.ui.drawing.DrawingCurve;
import miles.canvas.ui.fragment.DrawingFragment;
import miles.canvas.ui.fragment.DrawingFragment;
import miles.canvas.util.Logg;
import miles.canvas.util.ViewUtils;
import miles.canvas.ui.widget.CanvasSurface;
import miles.canvas.ui.widget.Fab;
import miles.canvas.ui.widget.RoundedFrameLayout;

/**
 * Created by milespeele on 8/7/15.
 */
public class CanvasLayout extends CoordinatorLayout implements
        FabMenu.ViewFabMenuListener, DrawingCurve.DrawingCurveListener, View.OnClickListener {

    @Bind(R.id.fragment_drawer_canvas) CanvasSurface drawer;
    @Bind(R.id.fragment_drawer_menu) FabMenu fabMenu;
    @Bind(R.id.fragment_drawer_animator) RoundedFrameLayout fabFrame;
    @Bind(R.id.fragment_drawer_options_menu) LinearLayout optionsMenu;
    @Bind(R.id.fragment_drawer_save_animation) LoadingAnimator loadingAnimator;

    @Inject EventBus bus;

    private Rect mRect = new Rect();
    private Paint mShadowPaint;
    private float mStartX, mStartY;
    private long mStartTime;
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
        ((MainApp) getContext().getApplicationContext()).getApplicationComponent().inject(this);
        bus.register(this);

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

        fabMenu.addListener(this);
        drawer.setListener(this);
    }

    @Override
    protected boolean drawChild(Canvas canvas, View child, long drawingTime) {
        if (mShadowPaint.getAlpha() != 0) {
            if (child == fabMenu || child == loadingAnimator) {
                canvas.drawCircle(fabMenu.getCenterX(),
                        fabMenu.getCenterY() + (getHeight() - fabMenu.getHeight()),
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
                    if (mShadowPaint.getAlpha() == 0) {
                        drawer.setEnabled(true);
                        fabMenu.setEnabled(true);
                    }
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
    public void onFabMenuButtonClicked(Fab v) {
        v.performClick();

        Fab eraser = fabMenu.eraser;

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
                eraser.setSelected(!eraser.isSelected());

                makeDrawingVisible();
                break;
            case R.id.menu_ink:
                ink();

                makeDrawingVisible();
                break;
            case R.id.menu_navigation:
                mHandler.removeCallbacksAndMessages(null);
                break;
        }

        if (v.getId() != R.id.menu_toggle) {
            if (v.getId() != R.id.menu_erase && fabMenu.eraser.isSelected()) {
                eraser.setSelected(false);
            }
        }
    }

    @Override
    public void toggleOptionsMenuVisibilty(boolean setVisible, DrawingCurve.State state) {
        if (setVisible) {
            TypefaceButton option1 = (TypefaceButton) optionsMenu.getChildAt(1);
            TypefaceButton option2 = (TypefaceButton) optionsMenu.getChildAt(2);
            if (state == DrawingCurve.State.TEXT) {
                option1.setText(R.string.view_options_menu_edit_text);
                option1.setCompoundDrawablesWithIntrinsicBounds(null, null, null,
                        getResources().getDrawable(R.drawable.ic_text_format_24dp));

                option2.setText(R.string.view_options_menu_edit_color);
                option2.setCompoundDrawablesWithIntrinsicBounds(null, null, null,
                        getResources().getDrawable(R.drawable.ic_palette_24dp));
            } else {
                option1.setText(R.string.view_options_menu_edit_camera);
                option1.setCompoundDrawablesWithIntrinsicBounds(null, null, null,
                        getResources().getDrawable(R.drawable.ic_camera_alt_24dp));

                option2.setText(R.string.view_options_menu_edit_import);
                option2.setCompoundDrawablesWithIntrinsicBounds(null, null, null,
                        getResources().getDrawable(R.drawable.ic_photo_24dp));
            }

            ViewUtils.visible(optionsMenu);
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
    public void surfaceReady() {
        Animator reveal = ViewAnimationUtils.createCircularReveal(this,
                getWidth() / 2, getHeight() / 2, 0, getHeight());
        reveal.setDuration(600);
        reveal.setInterpolator(new AccelerateDecelerateInterpolator());
        reveal.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                drawer.setVisibility(View.VISIBLE);
                fabMenu.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                HomeActivity activity = (HomeActivity) getContext();
                Window window = activity.getWindow();
                window.setBackgroundDrawable(null);
                activity.onLoadFinished();
            }
        });
        reveal.start();
    }

    @Override
    @OnClick({R.id.view_options_menu_accept, R.id.view_options_menu_1, R.id.view_options_menu_2,
                R.id.view_options_menu_cancel})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.view_options_menu_accept:
                drawer.onOptionsMenuAccept();
                break;
            case R.id.view_options_menu_cancel:
                drawer.onOptionsMenuCancel();
                break;
            default:
                HomeActivity activityHome = (HomeActivity) getContext();
                if (activityHome != null) {
                    activityHome.onOptionsMenuClicked(v, drawer.getState());
                }
                break;
        }
    }

    public void onEvent(EventTextChosen eventTextChosen) {
        makeDrawingVisible();
    }

    public void onEvent(EventBitmapChosen eventBitmapChosen) {
        makeDrawingVisible();
    }

    public void onEvent(EventColorChosen eventColorChosen) {
        makeDrawingVisible();
    }

    public void onEvent(EventFilenameChosen eventFilenameChosen) {
        makeDrawingVisible();
    }

    public void onEvent(EventClearCanvas eventClearCanvas) {
        makeDrawingVisible();
    }

    private void makeDrawingVisible() {
        drawer.setEnabled(true);
        hideBackground();
        fabMenu.toggleMenu();
    }

    public void startSaveBitmapAnimation() {
        loadingAnimator.setColors(drawer.getBackgroundColor());
        loadingAnimator.setTranslationY(getHeight());

        AnimatorSet animatorSet = new AnimatorSet();

        Animator alpha = ObjectAnimator.ofInt(this, CanvasLayout.ALPHA, 128);
        alpha.setInterpolator(new LinearInterpolator());
        alpha.setDuration(500);

        ObjectAnimator yPosition = ObjectAnimator.ofFloat(loadingAnimator, View.TRANSLATION_Y, 0);
        yPosition.setDuration(500);
        yPosition.setInterpolator(new DecelerateInterpolator());
        yPosition.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                loadingAnimator.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                loadingAnimator.startAnimation();
            }
        });

        animatorSet.playTogether(alpha, yPosition);
        animatorSet.start();
    }

    public void stopSaveAnimation(AnimatorListenerAdapter adapter) {
        loadingAnimator.stopAnimation(adapter);
    }

    public void showBackground() {
        Animator alpha = ObjectAnimator.ofInt(this, ALPHA, 128);
        alpha.setDuration(400);

        Animator radius = ObjectAnimator.ofFloat(this, RADIUS, getHeight());
        radius.setDuration(400);

        AnimatorSet set = new AnimatorSet();
        set.playTogether(alpha, radius);
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                drawer.setEnabled(false);
            }
        });
        set.start();
    }

    public void hideBackground() {
        Animator alpha = ObjectAnimator.ofInt(this, ALPHA, 0);
        alpha.setDuration(400);

        Animator radius = ObjectAnimator.ofFloat(this, RADIUS, 0);
        radius.setDuration(400);

        AnimatorSet set = new AnimatorSet();
        set.playTogether(alpha, radius);
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                drawer.setEnabled(true);
            }
        });
        set.start();
    }

    public void setListeners(DrawingFragment fragmentDrawer) {
        fabMenu.addListener(fragmentDrawer);
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

    private void setRadius(float radius) {
        mRadius = radius;
        invalidate();
    }

    private float getRadius() {
        return mRadius;
    }

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

    public static final Property<CanvasLayout, Integer> ALPHA = new ViewUtils.IntProperty<CanvasLayout>("alpha") {

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