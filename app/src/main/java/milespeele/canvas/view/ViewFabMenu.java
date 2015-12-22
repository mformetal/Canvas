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
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnticipateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.OvershootInterpolator;

import java.util.ArrayList;
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

    @Bind({R.id.menu_save, R.id.menu_text, R.id.menu_stroke_color, R.id.menu_canvas_color,
            R.id.menu_ink, R.id.menu_brush, R.id.menu_undo, R.id.menu_redo, R.id.menu_erase,
            R.id.menu_profile, R.id.menu_settings, R.id.menu_layers})
    List<ViewFab> buttonsList;

    @Inject EventBus bus;

    private Paint mPaint;
    private Matrix mRotateMatrix;
    private Circle mCircle;
    private ArrayList<ItemPosition> mItemPositions;
    private GestureDetector mFlingListener;
    private static final Interpolator OVERSHOOT_INTERPOLATOR = new OvershootInterpolator();
    private static final Interpolator ANTICIPATE_INTERPOLATOR = new AnticipateInterpolator();

    private boolean isMenuShowing = true;
    private boolean isAnimating = false;
    private boolean isFadedOut = false;
    private boolean isRotating = false;
    private float radius;
    private double mLastAngle;
    private float mMaxRadius;
    private float mItemRadius;
    private float[] matrixAngle = new float[9];
    private final static int VISIBILITY_DURATION = 350;
    private final static int INITIAL_DELAY = 0;
    private final static int DURATION = 400;
    private final static int DELAY_INCREMENT = 15;
    private final static int HIDE_DIFF = 50;
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

        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setAlpha(255);
        mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OVER));
        mPaint.setColor(getResources().getColor(R.color.primary_dark));

        mFlingListener = new GestureDetector(getContext(), new FlingListener());

        mRotateMatrix = new Matrix();

        setWillNotDraw(false);
        setDescendantFocusability(FOCUS_AFTER_DESCENDANTS);
        setBackground(null);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        ButterKnife.bind(this);
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

        mMaxRadius = toggle.getMeasuredHeight() * 4;
        radius = mMaxRadius;

        mCircle = new Circle(ViewUtils.centerX(toggle), ViewUtils.centerY(toggle), radius);

        mItemRadius = toggle.getMeasuredHeight() * 3;
        final int count = getChildCount();
        final double slice = Math.toRadians(360 / count);

        mItemPositions = new ArrayList<>();

        for (int i = count - 2; i >= 0; i--) {
            final ViewFab child = (ViewFab) getChildAt(i);

            double angle = i * slice;
            double x = mCircle.getCenterX() + mItemRadius * Math.cos(angle);
            double y = mCircle.getCenterY() - mItemRadius * Math.sin(angle);

            child.layout((int) x - child.getMeasuredWidth() / 2,
                    (int) y - child.getMeasuredHeight() / 2,
                    (int) x + child.getMeasuredWidth() / 2,
                    (int) y + child.getMeasuredHeight() / 2);

            mItemPositions.add(new ItemPosition(child, x, y, ViewUtils.radius(child)));
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
            case R.id.menu_stroke_color:
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
                inker.toggleScaled();
                break;
        }

        if (v.getId() != R.id.menu_erase && eraser.isScaledUp()) {
            eraser.scaleDown();
            parent.erase();
        }

        if (v.getId() != R.id.menu_ink && inker.isScaledUp()) {
            inker.scaleDown();
            parent.ink();
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        final float x = event.getX(), y = event.getY();

        if (isAnimating) {
            return false;
        }

        if (isFadedOut || !isMenuShowing) {
            if (Circle.contains(mCircle.getCenterX() - x, mCircle.getCenterY() - y, ViewUtils.radius(toggle))) {
                onItemClicked(toggle);
            }
            return false;
        }

        if (!mCircle.contains(x, y)) {
            return false;
        }

//        mFlingListener.onTouchEvent(event);

        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                getClickedItem(x, y);
                mLastAngle = mCircle.angleInDegrees(x, y);
                break;
            case MotionEvent.ACTION_MOVE:
                isRotating = true;

                double degrees = mCircle.angleInDegrees(x, y);
                double rotater = degrees - mLastAngle;

                for (ItemPosition itemPosition: mItemPositions) {
                    itemPosition.update(rotater);
                }

                mLastAngle = degrees;
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                isRotating = false;
                break;
        }

        invalidate();

        return true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawCircle(getCenterX(), getCenterY(), radius, mPaint);
    }

    private double getMatrixAngle() {
        mRotateMatrix.getValues(matrixAngle);
        double angle = Math.atan2(matrixAngle[Matrix.MSKEW_X], matrixAngle[Matrix.MSCALE_X]) * (180f / Math.PI);
        if (angle < 0) {
            angle = 360 - Math.abs(angle);
        }
        return angle;
    }

    private void getClickedItem(float x, float y) {
        if (Circle.contains(getCenterX() - x, getCenterY() - y, ViewUtils.radius(toggle))) {
            onItemClicked(toggle);
            return;
        }

        for (ItemPosition position: mItemPositions) {
            if (position.contains(x, y)) {
                onItemClicked(position.mView);
            }
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

            ObjectAnimator background = ObjectAnimator.ofFloat(this, RADIUS_ANIMATOR, mMaxRadius);
            background.setDuration(DURATION + DELAY_INCREMENT * buttonsList.size());
            background.setInterpolator(OVERSHOOT_INTERPOLATOR);

            int delay = INITIAL_DELAY;
            for (ItemPosition position: mItemPositions) {
                float diffX = position.mItemCircle.getCenterX() - getCenterX();
                float diffY = position.mItemCircle.getCenterY() - getCenterY();

                View view = position.mView;

                AnimatorSet out = new AnimatorSet();
                out.playTogether(
                        ObjectAnimator.ofFloat(view, View.X, view.getX() + diffX),
                        ObjectAnimator.ofFloat(view, View.Y, view.getY() + diffY),
                        ObjectAnimator.ofFloat(view, View.ALPHA, 0.0f, 1.0f));
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

            ObjectAnimator background = ObjectAnimator.ofFloat(this, RADIUS_ANIMATOR, 0);
            background.setDuration(HIDE_DIFF + DURATION + DELAY_INCREMENT * buttonsList.size());
            background.setInterpolator(ANTICIPATE_INTERPOLATOR);

            int delay = INITIAL_DELAY;
            for (ItemPosition position: mItemPositions) {
                float diffX = position.mItemCircle.getCenterX() - getCenterX();
                float diffY = position.mItemCircle.getCenterY() - getCenterY();

                View view = position.mView;

                AnimatorSet out = new AnimatorSet();
                out.playTogether(
                        ObjectAnimator.ofFloat(view, View.X, view.getX() - diffX),
                        ObjectAnimator.ofFloat(view, View.Y, view.getY() - diffY),
                        ObjectAnimator.ofFloat(view, View.ALPHA, 1.0f, 0.0f));
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

            ObjectAnimator fade = ObjectAnimator.ofObject(mPaint, ViewUtils.ALPHA, new ArgbEvaluator(),
                    mPaint.getAlpha(), 0)
                    .setDuration(VISIBILITY_DURATION);
            fade.addUpdateListener(animation -> invalidate());
            fade.start();

            ButterKnife.apply(buttonsList, GONE);
        }
    }

    public void fadeIn() {
        if (isFadedOut) {
            isFadedOut = false;

            ObjectAnimator fade = ObjectAnimator.ofObject(mPaint, ViewUtils.ALPHA, new ArgbEvaluator(),
                    mPaint.getAlpha(), 255)
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

    public float getRadius() {
        return radius;
    }

    public void setRadius(float radius) {
        this.radius = radius;
        invalidate(ViewUtils.boundingRect(mCircle.getCenterX(), mCircle.getCenterY(), radius));
    }

    public float getCenterX() { return mCircle.getCenterX(); }

    public float getCenterY() { return mCircle.getCenterY(); }

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

        private Circle mItemCircle;
        private ViewFab mView;

        public ItemPosition(ViewFab child, double itemX, double itemY, float radius) {
            mView = child;
            mItemCircle = new Circle((float) itemX, (float) itemY, radius);
        }

        public void update(double matrixAngle) {
            double angleInRads = Math.toRadians(matrixAngle);

            double cosAngle = Math.cos(angleInRads);
            double sinAngle = Math.sin(angleInRads);

            float dx = mItemCircle.getCenterX() - getCenterX();
            float dy = mItemCircle.getCenterY() - getCenterY();

            float rx = (float) (dx * cosAngle - dy * sinAngle);
            float ry = (float) (dx * sinAngle + dy * cosAngle);

            rx += getCenterX();
            ry += getCenterY();

            mItemCircle.setCenterX(rx);
            mItemCircle.setCenterY(ry);

            float radius = mItemCircle.getRadius();

            mView.setX(rx - radius);
            mView.setY(ry - radius);
        }

        public boolean contains(float x, float y) {
            return mItemCircle.contains(x, y);
        }
    }

    private final class FlingListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            Logg.log("VELOCITY: ", velocityX, velocityY);
            return super.onFling(e1, e2, velocityX, velocityY);
        }
    }
}