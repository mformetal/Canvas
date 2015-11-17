package milespeele.canvas.view;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnticipateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.OvershootInterpolator;

import java.util.List;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnLongClick;
import de.greenrobot.event.EventBus;
import milespeele.canvas.MainApp;
import milespeele.canvas.R;
import milespeele.canvas.event.EventBrushChosen;
import milespeele.canvas.event.EventColorChosen;
import milespeele.canvas.paint.PaintStyles;
import milespeele.canvas.util.AbstractAnimatorListener;
import milespeele.canvas.util.Logg;
import milespeele.canvas.util.TextUtils;
import milespeele.canvas.util.ViewUtils;

/**
 * Created by milespeele on 8/7/15.
 */
public class ViewFabMenu extends ViewGroup implements View.OnClickListener, View.OnLongClickListener {

    @Bind(R.id.menu_toggle) ViewFab toggle;
    @Bind(R.id.menu_erase) ViewFab eraser;

    @Bind({R.id.menu_shape_chooser, R.id.menu_text, R.id.menu_colorize,  R.id.menu_color,
            R.id.menu_brush, R.id.menu_undo, R.id.menu_redo, R.id.menu_erase})
    List<ViewFab> buttonsList;

    @Inject EventBus bus;

    private ObjectAnimator toggleClose;
    private ObjectAnimator toggleOpen;
    private Paint backgroundPaint;
    private Bitmap background;
    private static final Interpolator OVERSHOOT_INTERPOLATOR = new OvershootInterpolator();
    private static final Interpolator ANTICIPATE_INTERPOLATOR = new AnticipateInterpolator();

    private boolean isMenuShowing = true;
    private boolean isAnimating = false;
    private boolean isMenuGone = false;
    private float radius;
    private float centreX, centreY;
    private static float MAX_RADIUS;
    private final static int VISIBILITY_DURATION = 350;
    private final static int DELAY = 0;
    private final static int DURATION = 400;
    private final static int DELAY_INCREMENT = 15;
    private final static int HIDE_DIFF = 50;

    private ViewFabMenuListener listener;

    public interface ViewFabMenuListener {
        void onFabMenuButtonClicked(ViewFab v);
    }

    public ViewFabMenu(Context context) {
        super(context);
        init();
    }

    public ViewFabMenu(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ViewFabMenu(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public ViewFabMenu(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        ((MainApp) getContext().getApplicationContext()).getApplicationComponent().inject(this);
        bus.register(this);

        backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        backgroundPaint.setAlpha(255);
        backgroundPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        backgroundPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OVER));
        backgroundPaint.setColor(getResources().getColor(R.color.primary_dark));

        setClickable(true);
        setWillNotDraw(false);
        setDescendantFocusability(FOCUS_AFTER_DESCENDANTS);
        setBackground(null);
        setDrawingCacheEnabled(true);
        setDrawingCacheQuality(DRAWING_CACHE_QUALITY_HIGH);
        buildDrawingCache();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        ButterKnife.bind(this);

        toggleClose = ObjectAnimator.ofFloat(toggle, "rotation", -135f, -270f);
        toggleClose.setDuration(Math.round(DURATION * 1.5));

        toggleOpen = ObjectAnimator.ofFloat(toggle, "rotation", 0f, -135f);
        toggleOpen.start();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        measureChildWithMargins(toggle, widthMeasureSpec, 0, heightMeasureSpec, 0);

        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            if (child == toggle) continue;
            measureChildWithMargins(child, widthMeasureSpec, 0, heightMeasureSpec, 0);
        }

        int width = getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec);

        setMeasuredDimension(width, width >> 1);
    }

    @Override
    protected LayoutParams generateDefaultLayoutParams() {
        return new MarginLayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
    }

    @Override
    protected LayoutParams generateLayoutParams(LayoutParams p) {
        return new MarginLayoutParams(p);
    }

    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new MarginLayoutParams(getContext(), attrs);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if (!changed) {
            return;
        }

        final int count = getChildCount();
        MarginLayoutParams lps = (MarginLayoutParams) toggle.getLayoutParams();

        toggle.layout(r / 2 - toggle.getMeasuredWidth() / 2,
                getMeasuredHeight() - toggle.getMeasuredHeight() - lps.bottomMargin,
                r / 2 + toggle.getMeasuredWidth() / 2,
                getMeasuredHeight() - lps.bottomMargin);

        final int radius = toggle.getMeasuredHeight() * 3;
        centreX = toggle.getX() + toggle.getWidth()  / 2;
        centreY = toggle.getY() + toggle.getHeight() / 2;
        final double slice = Math.PI / (count - 2);
        for (int i = count - 2; i >= 0; i--) {
            final View child = getChildAt(i);

            double angle = -(slice * (i));
            double x = centreX + radius * Math.cos(angle);
            double y = centreY + radius * Math.sin(angle);

            child.layout((int) x - child.getMeasuredWidth() / 2,
                    (int) y - child.getMeasuredHeight() / 2,
                    (int) x + child.getMeasuredWidth() / 2,
                    (int) y + child.getMeasuredHeight() / 2);
        }

        if (MAX_RADIUS == 0) {
            float highest = getChildAt(0).getY();
            for (int i = 0; i < getChildCount(); i++) {
                View v = getChildAt(i);
                if (v.getY() > highest) {
                    highest = v.getY();
                }
            }
            MAX_RADIUS = centreY;
            this.radius = MAX_RADIUS;
        }
    }

    @Override
    @OnClick({R.id.menu_colorize, R.id.menu_brush, R.id.menu_color, R.id.menu_undo,
        R.id.menu_redo, R.id.menu_erase, R.id.menu_toggle, R.id.menu_shape_chooser, R.id.menu_text})
    public void onClick(View v) {
        if (listener != null) {
            listener.onFabMenuButtonClicked((ViewFab) v);
        }

        v.performClick();
        ViewCanvasLayout parent = ((ViewCanvasLayout) getParent());
        switch (v.getId()) {
            case R.id.menu_toggle:
                toggleMenu();
                break;
            case R.id.menu_colorize:
                eraser.scaleDown();
                break;
            case R.id.menu_color:
                eraser.scaleDown();
                break;
            case R.id.menu_undo:
                parent.undo();
                break;
            case R.id.menu_redo:
                parent.redo();
                break;
            case R.id.menu_erase:
                parent.erase();
                eraser.toggleScaled();
                break;
        }
    }

    @Override
    @OnLongClick({R.id.menu_colorize, R.id.menu_brush, R.id.menu_color, R.id.menu_undo,
            R.id.menu_redo, R.id.menu_erase, R.id.menu_toggle, R.id.menu_shape_chooser, R.id.menu_text})
    public boolean onLongClick(View v) {
        return false;
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        canvas.drawCircle(centreX, centreY, radius, backgroundPaint);
        super.dispatchDraw(canvas);
    }

    public void setListener(ViewFabMenuListener otherListener) {
        listener = otherListener;
    }

    public void toggleMenu() {
        if (!isAnimating) {
            if (isMenuGone) {
                fadeIn();
            } else {
                if (isMenuShowing) {
                    hide();
                } else {
                    show();
                }
            }
        }
    }

    public void show() {
        if (!isMenuShowing && !isAnimating) {
            isAnimating = true;
            isMenuShowing = true;
            toggleOpen.start();

            ObjectAnimator background = ObjectAnimator.ofFloat(this, "radius", MAX_RADIUS);
            background.setDuration(DURATION + DELAY_INCREMENT * buttonsList.size());
            background.setInterpolator(OVERSHOOT_INTERPOLATOR);

            int delay = DELAY;
            for (ViewFab view: buttonsList) {
                float diffX = view.getX() - centreX, diffY = view.getY() - centreY;

                AnimatorSet out = new AnimatorSet();
                out.playTogether(ObjectAnimator.ofFloat(view, ViewUtils.TRANSLATION_X, diffX),
                        ObjectAnimator.ofFloat(view, ViewUtils.TRANSLATION_Y, diffY),
                        ObjectAnimator.ofFloat(view, ViewUtils.ALPHA, 0.0f, 1.0f));
                out.setStartDelay(delay);
                out.setDuration(DURATION);
                out.setInterpolator(OVERSHOOT_INTERPOLATOR);
                out.addListener(new AbstractAnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        view.setVisibility(View.VISIBLE);
                        if (view == buttonsList.get(0)) {
                            background.start();
                        }
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        if (view == buttonsList.get(buttonsList.size() - 1)) {
                            isAnimating = false;
                        }
                    }
                });
                out.start();
                delay += DELAY_INCREMENT;
            }
        }
    }

    public void hide() {
        if (isMenuShowing && !isAnimating) {
            isAnimating = true;
            isMenuShowing = false;
            toggleClose.start();

            ObjectAnimator background = ObjectAnimator.ofFloat(this, "radius", 0);
            background.setDuration(HIDE_DIFF + DURATION + DELAY_INCREMENT * buttonsList.size());
            background.setInterpolator(ANTICIPATE_INTERPOLATOR);

            int delay = DELAY;
            for (ViewFab view: buttonsList) {
                float diffX = view.getX() - centreX, diffY = view.getY() - centreY;

                AnimatorSet out = new AnimatorSet();
                out.playTogether(ObjectAnimator.ofFloat(view, ViewUtils.TRANSLATION_Y, -diffY),
                        ObjectAnimator.ofFloat(view, ViewUtils.TRANSLATION_X, -diffX),
                        ObjectAnimator.ofFloat(view, ViewUtils.ALPHA, 1.0f, 0.0f));
                out.setStartDelay(delay);
                out.setDuration(DURATION);
                out.setInterpolator(ANTICIPATE_INTERPOLATOR);
                out.addListener(new AbstractAnimatorListener() {

                    @Override
                    public void onAnimationStart(Animator animation) {
                        if (view == buttonsList.get(0)) {
                            background.start();
                        }
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        view.setVisibility(View.GONE);
                        if (view == buttonsList.get(buttonsList.size() - 1)) {
                            isAnimating = false;
                        }
                    }
                });
                out.start();
                delay += DELAY_INCREMENT;
            }
        }
    }

    public void fadeOut() {
        if (!isMenuGone && isMenuShowing) {
            isMenuGone = true;
            toggleClose.start();

            ObjectAnimator fade = ObjectAnimator.ofObject(backgroundPaint, ViewUtils.ALPHA, new ArgbEvaluator(),
                    backgroundPaint.getAlpha(), 0)
                    .setDuration(VISIBILITY_DURATION);
            fade.addUpdateListener(animation -> invalidate());
            fade.start();

            ButterKnife.apply(buttonsList, GONE);
        }
    }

    public void fadeIn() {
        if (isMenuGone) {
            isMenuGone = false;
            toggleOpen.start();

            ObjectAnimator fade = ObjectAnimator.ofObject(backgroundPaint, ViewUtils.ALPHA, new ArgbEvaluator(),
                    backgroundPaint.getAlpha(), 255)
                    .setDuration(VISIBILITY_DURATION);
            fade.addUpdateListener(animation -> invalidate());
            fade.start();

            ButterKnife.apply(buttonsList, VISIBLE);
        }
    }

    public void onEvent(EventColorChosen eventColorChosen) {
        if (eventColorChosen.color != 0) {
            eraser.scaleDown();
        }
    }

    public void onEvent(EventBrushChosen eventBrushChosen) {
        if (eventBrushChosen.paint != null) {
            eraser.scaleDown();
        }
    }

    public boolean isVisible() {
        return !isMenuGone;
    }

    public float getCircleRadius() {
        return MAX_RADIUS;
    }

    public float getRadius() {
        return radius;
    }

    public void setRadius(float radius) {
        this.radius = radius;
        invalidate();
    }

    private static final ButterKnife.Action<View> GONE = new ButterKnife.Action<View>() {

        @Override
        public void apply(View view, int index) {
            ObjectAnimator gone = ObjectAnimator.ofFloat(view, ViewUtils.ALPHA, 1f, 0f);
            gone.setDuration(VISIBILITY_DURATION);
            gone.addListener(new AbstractAnimatorListener() {

                @Override
                public void onAnimationEnd(Animator animation) {
                    view.setVisibility(View.GONE);
                }
            });
            gone.start();
        }
    };

    private static final ButterKnife.Action<View> VISIBLE = new ButterKnife.Action<View>() {
        @Override
        public void apply(View view, int index) {
            ObjectAnimator gone = ObjectAnimator.ofFloat(view, ViewUtils.ALPHA, 0f, 1f);
            gone.setDuration(VISIBILITY_DURATION);
            gone.addListener(new AbstractAnimatorListener() {

                @Override
                public void onAnimationStart(Animator animation) {
                    view.setVisibility(View.VISIBLE);
                }
            });
            gone.start();
        }
    };
}