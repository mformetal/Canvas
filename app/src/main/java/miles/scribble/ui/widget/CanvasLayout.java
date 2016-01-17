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
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.Property;
import android.view.MotionEvent;
import android.view.SoundEffectConstants;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.LinearLayout;
import android.widget.Toolbar;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;
import miles.scribble.MainApp;
import miles.scribble.R;
import miles.scribble.data.event.EventBitmapChosen;
import miles.scribble.data.event.EventClearCanvas;
import miles.scribble.data.event.EventColorChosen;
import miles.scribble.data.event.EventFilenameChosen;
import miles.scribble.data.event.EventTextChosen;
import miles.scribble.rx.SafeSubscription;
import miles.scribble.ui.activity.BaseActivity;
import miles.scribble.ui.activity.HomeActivity;
import miles.scribble.ui.drawing.DrawingCurve;
import miles.scribble.util.FileUtils;
import miles.scribble.util.Logg;
import miles.scribble.util.ViewUtils;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by milespeele on 8/7/15.
 */
public class CanvasLayout extends CoordinatorLayout implements
        CircleFabMenu.ViewFabMenuListener, DrawingCurve.DrawingCurveListener,
        View.OnClickListener {

    @Bind(R.id.fragment_drawer_canvas) CanvasSurface drawer;
    @Bind(R.id.fragment_drawer_menu) CircleFabMenu fabMenu;
    @Bind(R.id.fragment_drawer_animator) RoundedFrameLayout fabFrame;
    @Bind(R.id.fragment_drawer_text_bitmap) LinearLayout textAndBitmapOptions;
    @Bind(R.id.fragment_drawer_loading) LoadingAnimator loadingAnimator;
    @Bind(R.id.fragment_drawer_toolbar) Toolbar toolbar;

    @Inject EventBus bus;

    private Rect mRect = new Rect();
    private Paint mShadowPaint;
    private float mStartX, mStartY;
    private long mStartTime;
    private Handler mHandler;
    private float mRadius;

    private CanvasLayoutListener mListener;

    public interface CanvasLayoutListener {
        void onFabMenuButtonClicked(View view);
        void onOptionsMenuButtonClicked(View view, DrawingCurve.State state);
        void onNavigationIconClicked();
    }

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
        toolbar.setNavigationOnClickListener(v -> {
            if (mListener != null) {
                mListener.onNavigationIconClicked();
            }
        });

        getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                getViewTreeObserver().removeOnPreDrawListener(this);
                loadingAnimator.startAnimation();
                return false;
            }
        });
    }

    @Override
    protected boolean drawChild(Canvas canvas, View child, long drawingTime) {
        if (mShadowPaint.getAlpha() != 0) {
            if (child == fabMenu) {
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
                if (isSystemUISwipe(ev)) {
                    return true;
                }

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
                    if (mShadowPaint.getAlpha() != 0) {
                        drawer.setEnabled(false);
                    }

                    if (!fabMenu.isEnabled()) {
                        fabMenu.setEnabled(true);
                    }
                }
                break;
            case MotionEvent.ACTION_MOVE:
                mHandler.postDelayed(() -> ViewUtils.systemUIGone(getRootView()), 350);
                break;
        }

        return false;
    }

    @Override
    public void onFabMenuButtonClicked(Fab v) {
        mListener.onFabMenuButtonClicked(v);

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
            TypefaceButton option1 = (TypefaceButton) textAndBitmapOptions.getChildAt(1);
            TypefaceButton option2 = (TypefaceButton) textAndBitmapOptions.getChildAt(2);
            if (state == DrawingCurve.State.TEXT) {
                option1.setText(R.string.view_options_menu_edit_text);
                option1.setCompoundDrawablesWithIntrinsicBounds(null, null, null,
                        ContextCompat.getDrawable(getContext(), R.drawable.ic_text_format_24dp));

                option2.setText(R.string.view_options_menu_edit_color);
                option1.setCompoundDrawablesWithIntrinsicBounds(null, null, null,
                        ContextCompat.getDrawable(getContext(), R.drawable.ic_palette_24dp));
            } else {
                option1.setText(R.string.view_options_menu_edit_camera);
                option1.setCompoundDrawablesWithIntrinsicBounds(null, null, null,
                        ContextCompat.getDrawable(getContext(), R.drawable.ic_camera_alt_24dp));

                option2.setText(R.string.view_options_menu_edit_import);
                option1.setCompoundDrawablesWithIntrinsicBounds(null, null, null,
                        ContextCompat.getDrawable(getContext(), R.drawable.ic_photo_24dp));
            }

            ViewUtils.visible(textAndBitmapOptions);
        } else {
            ViewUtils.gone(textAndBitmapOptions);
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
        loadingAnimator.stopAnimation(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                ViewUtils.gone(loadingAnimator);

                Animator reveal = ViewAnimationUtils.createCircularReveal(CanvasLayout.this,
                        getWidth() / 2, getHeight() / 2, 0, getHeight());
                reveal.setDuration(600);
                reveal.setInterpolator(new AccelerateDecelerateInterpolator());
                reveal.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        drawer.setVisibility(View.VISIBLE);
                        fabMenu.setVisibility(View.VISIBLE);
                    }
                });
                reveal.start();
            }
        }, 500);
    }

    public void unrevealAndSave() {
        Animator reveal = ViewAnimationUtils.createCircularReveal(this,
                getWidth() / 2, getHeight() / 2, getHeight(), 0);
        reveal.setDuration(600);
        reveal.setInterpolator(new AccelerateDecelerateInterpolator());
        reveal.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);

                drawer.setVisibility(View.GONE);
                fabMenu.setVisibility(View.GONE);

                loadingAnimator.startAnimation();

                final HomeActivity activity = (HomeActivity) getContext();

                SafeSubscription<byte[]> subscriber = new SafeSubscription<byte[]>(activity) {
                    @Override
                    public void onCompleted() {
                        loadingAnimator.stopAnimation(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                activity.finishAndRemoveTask();
                            }
                        }, 700);
                    }

                    @Override
                    public void onError(Throwable e) {
                        Logg.log(e);
                    }
                };

                FileUtils.cache(getDrawerBitmap(), activity)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(subscriber);
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
                mListener.onOptionsMenuButtonClicked(v, drawer.getState());
                break;
        }
    }

    public void setActivityListener(HomeActivity activityListener) {
        mListener = activityListener;
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
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                drawer.setEnabled(true);
                undim();
                fabMenu.toggleMenu();
            }
        }, 200);
    }

    public void dim() {
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

    public void undim() {
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

    public Bitmap getDrawerBitmap() {
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
        if (event.getY() <= getResources().getDimension(R.dimen.system_ui_scrim)) {
            return true;
        }

        if (event.getY() >=
                getHeight() - getResources().getDimension(R.dimen.system_ui_scrim)) {
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