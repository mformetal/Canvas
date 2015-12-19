package milespeele.canvas.view;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
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
import de.greenrobot.event.EventBus;
import milespeele.canvas.MainApp;
import milespeele.canvas.R;
import milespeele.canvas.event.EventBrushChosen;
import milespeele.canvas.event.EventColorChosen;
import milespeele.canvas.event.EventFilenameChosen;
import milespeele.canvas.event.EventParseError;
import milespeele.canvas.util.AbstractAnimatorListener;
import milespeele.canvas.util.Circle;
import milespeele.canvas.util.Logg;
import milespeele.canvas.util.ViewUtils;


/**
 * Created by milespeele on 8/7/15.
 */
public class ViewFabMenu extends ViewGroup {

    @Bind(R.id.menu_toggle) ViewFab toggle;
    @Bind(R.id.menu_erase) ViewFab eraser;
    @Bind(R.id.menu_save) ViewFab saver;
    @Bind(R.id.menu_ink) ViewFab inker;

    @Bind({R.id.menu_save, R.id.menu_canvas_color, R.id.menu_text,  R.id.menu_color, R.id.menu_brush,
            R.id.menu_ink, R.id.menu_undo, R.id.menu_redo, R.id.menu_erase})
    List<ViewFab> buttonsList;

    @Inject EventBus bus;

    private ObjectAnimator toggleClose, toggleOpen;
    private Paint backgroundPaint;
    private Matrix rotateMatrix;
    private Circle circle;
    private ItemPosition[] itemPositions;
    private static final Interpolator OVERSHOOT_INTERPOLATOR = new OvershootInterpolator();
    private static final Interpolator ANTICIPATE_INTERPOLATOR = new AnticipateInterpolator();

    private boolean isMenuShowing = true;
    private boolean isAnimating = false;
    private boolean isFadedOut = false;
    private boolean isRotating = false;
    private float radius;
    private float centerX, centerY;
    private float lastX, lastY;
    private float startAngle;
    private float maxRadius;
    private final static int VISIBILITY_DURATION = 350;
    private final static int INITIAL_DELAY = 0;
    private final static int DURATION = 400;
    private final static int DELAY_INCREMENT = 15;
    private final static int HIDE_DIFF = 50;
    private static final float TOLERANCE = 5f;
    private static final String RADIUS_ANIMATOR = "radius";

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

        rotateMatrix = new Matrix();

        setWillNotDraw(false);
        setDescendantFocusability(FOCUS_AFTER_DESCENDANTS);
        setBackground(null);
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

        MarginLayoutParams lps = (MarginLayoutParams) toggle.getLayoutParams();

        toggle.layout(r / 2 - toggle.getMeasuredWidth() / 2,
                getMeasuredHeight() - toggle.getMeasuredHeight() - lps.bottomMargin,
                r / 2 + toggle.getMeasuredWidth() / 2,
                getMeasuredHeight() - lps.bottomMargin);

        centerX = ViewUtils.centerX(toggle);
        centerY = ViewUtils.centerY(toggle);

        maxRadius = toggle.getMeasuredHeight() * 4;
        radius = maxRadius;

        circle = new Circle(centerX, centerY, radius);

        final int itemRad = toggle.getMeasuredHeight() * 3;
        final int children = getChildCount() - 2;
        final double slice = Math.PI / children;

        itemPositions = new ItemPosition[children + 1];

        for (int i = children; i >= 0; i--) {
            final View child = getChildAt(i);

            double angle = slice * i;
            double x = centerX + itemRad * Math.cos(angle);
            double y = centerY - itemRad * Math.sin(angle);

            child.layout((int) x - child.getMeasuredWidth() / 2,
                    (int) y - child.getMeasuredHeight() / 2,
                    (int) x + child.getMeasuredWidth() / 2,
                    (int) y + child.getMeasuredHeight() / 2);
            itemPositions[i] = new ItemPosition(child, (float) x, (float) y, (float) child.getMeasuredWidth() / 2);
        }
    }

    public void onItemClicked(View v) {
        if (listener != null) {
            listener.onFabMenuButtonClicked((ViewFab) v);
        }

        v.performClick();
        ViewCanvasLayout parent = ((ViewCanvasLayout) getParent());
        switch (v.getId()) {
            case R.id.menu_toggle:
                toggleMenu();
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
            case R.id.menu_ink:
                parent.ink();
                break;
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        final float x = event.getX(), y = event.getY();

        if (isAnimating) {
            return false;
        }

        if (isFadedOut || !isMenuShowing) {
            if (Circle.contains(centerX - x, centerY - y, ViewUtils.radius(toggle))) {
                onItemClicked(toggle);
            }
            return false;
        }

        return circle.contains(x, y);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        final float x = event.getX(), y = event.getY();

        if (isFadedOut || !isMenuShowing) {
            return false;
        }

        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                getClickedItem(x, y);
                startAngle = circle.angle(x, y);
                break;
            case MotionEvent.ACTION_MOVE:
                isRotating = true;

                lastX = x;
                lastY = y;

                float rotation = circle.angle(x, y);
                float angle = -(startAngle - rotation);
                rotateMatrix.postRotate(angle, centerX, centerY);

                startAngle = rotation;
                updateItemPositions(angle);
                break;
            case MotionEvent.ACTION_UP:
                isRotating = false;
                break;
        }

        invalidate();

        return true;
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        canvas.drawCircle(centerX, centerY, radius, backgroundPaint);
        canvas.concat(rotateMatrix);
        super.dispatchDraw(canvas);
    }

    private void getClickedItem(float x, float y) {
        if (Circle.contains(centerX - x, centerY - y, ViewUtils.radius(toggle))) {
            onItemClicked(toggle);
            return;
        }

        for (ItemPosition position: itemPositions) {
            if (position.contains(x, y)) {
                onItemClicked(position.view);
            }
        }
    }

    private void updateItemPositions(float angle) {
        for (ItemPosition itemPosition: itemPositions) {
            itemPosition.update(angle);
        }
    }

    public void setListener(ViewFabMenuListener otherListener) {
        listener = otherListener;
    }

    public void toggleMenu() {
        if (!isAnimating) {
            if (isFadedOut) {
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

            ObjectAnimator background = ObjectAnimator.ofFloat(this, RADIUS_ANIMATOR, maxRadius);
            background.setDuration(DURATION + DELAY_INCREMENT * buttonsList.size());
            background.setInterpolator(OVERSHOOT_INTERPOLATOR);

            int delay = INITIAL_DELAY;
            for (ViewFab view: buttonsList) {
                float diffX = view.getX() - centerX, diffY = view.getY() - centerY;

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

            ObjectAnimator background = ObjectAnimator.ofFloat(this, RADIUS_ANIMATOR, 0);
            background.setDuration(HIDE_DIFF + DURATION + DELAY_INCREMENT * buttonsList.size());
            background.setInterpolator(ANTICIPATE_INTERPOLATOR);

            int delay = INITIAL_DELAY;
            for (ViewFab view: buttonsList) {
                float diffX = view.getX() - centerX, diffY = view.getY() - centerY;

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
        if (!isFadedOut && isMenuShowing) {
            isFadedOut = true;
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
        if (isFadedOut) {
            isFadedOut = false;
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

    public void onEvent(EventParseError eventParseError) {
        saver.stopPulse();
    }

    public void onEvent(EventFilenameChosen eventFilenameChosen) {
        saver.startPulse();
    }

    public boolean isVisible() {
        return !isFadedOut && isMenuShowing;
    }

    public float getCircleRadius() {
        return maxRadius;
    }

    public float getRadius() {
        return radius;
    }

    public void setRadius(float radius) {
        this.radius = radius;
        invalidate(ViewUtils.boundingRect(centerX, centerY, radius));
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

    private class ItemPosition {

        private Circle itemCircle;
        private View view;

        public ItemPosition(View child, float itemX, float itemY, float radius) {
            itemCircle = new Circle(itemX, itemY, radius);
            view = child;
        }

        public void update(double newAngle) {
            double cos = Math.cos(newAngle);
            double sin = Math.sin(newAngle);

//            itemCircle.setCenterX((float) x);
//            itemCircle.setCenterY((float) y);
        }

        public boolean contains(float x, float y) {
            return itemCircle.contains(x, y);
        }
    }
}