package milespeele.canvas.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
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
import java.util.Collections;
import java.util.Comparator;
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
import milespeele.canvas.util.Circle;
import milespeele.canvas.util.NetworkUtils;
import milespeele.canvas.util.ViewUtils;


/**
 * Created by milespeele on 8/7/15.
 */
public class ViewFabMenu extends ViewGroup implements View.OnClickListener {

    @Bind(R.id.menu_toggle) ViewFab toggle;
    @Bind(R.id.menu_erase) ViewFab eraser;
    @Bind(R.id.menu_upload) ViewFab saver;

    @Bind({R.id.menu_upload, R.id.menu_text, R.id.menu_stroke_color, R.id.menu_canvas_color,
            R.id.menu_ink, R.id.menu_brush, R.id.menu_undo, R.id.menu_redo, R.id.menu_erase,
            R.id.menu_image, R.id.menu_settings})
    List<ViewFab> buttonsList;

    @Inject EventBus bus;

    private Paint mPaint;
    private Circle mCircle;
    private ViewFab mClickedItem;
    private ArrayList<ItemPosition> mItemPositions;
    private GestureDetector mGestureDetector;
    private static final Interpolator OVERSHOOT_INTERPOLATOR = new OvershootInterpolator();
    private static final Interpolator ANTICIPATE_INTERPOLATOR = new AnticipateInterpolator();

    private boolean isMenuShowing = true;
    private boolean isAnimating = false;
    private boolean isDragging = false;
    private boolean isFlinging = false;
    private float radius;
    private double mLastAngle;
    private float mMaxRadius;
    private float mStartY;
    private final static int INITIAL_DELAY = 0;
    private final static int DURATION = 400;
    private final static int DELAY_INCREMENT = 15;
    private final static int HIDE_DIFF = 50;

    private ArrayList<ViewFabMenuListener> mListeners;
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

        mListeners = new ArrayList<>();

        mItemPositions = new ArrayList<>();

        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mPaint.setColor(getResources().getColor(R.color.half_opacity_gray));

        mGestureDetector = new GestureDetector(getContext(), new GestureListener());

        setWillNotDraw(false);
        setDescendantFocusability(FOCUS_AFTER_DESCENDANTS);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        ButterKnife.bind(this);
        rotateToggleClosed();
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
        if (!mItemPositions.isEmpty()) {
            return;
        }

        MarginLayoutParams lps = (MarginLayoutParams) toggle.getLayoutParams();

        toggle.layout(r / 2 - toggle.getMeasuredWidth() / 2,
                getMeasuredHeight() - toggle.getMeasuredHeight() - lps.bottomMargin,
                r / 2 + toggle.getMeasuredWidth() / 2,
                getMeasuredHeight() - lps.bottomMargin);

        mMaxRadius = toggle.getMeasuredHeight() * 3.75f;
        radius = mMaxRadius;

        mCircle = new Circle(ViewUtils.relativeCenterX(toggle), ViewUtils.relativeCenterY(toggle), radius);
        mItemPositions.add(new ItemPosition(toggle, getCenterX(), getCenterY(), ViewUtils.radius(toggle)));

        float mItemRadius = toggle.getMeasuredHeight() * 3;
        final int count = getChildCount();
        final double slice = Math.toRadians(360d / (count - 1));

        for (int i = 0; i < count; i++) {
            final ViewFab child = (ViewFab) getChildAt(i);
            if (child.getId() != R.id.menu_toggle) {
                double angle = i * slice;
                double x = getCenterX() + mItemRadius * Math.cos(angle);
                double y = getCenterY() - mItemRadius * Math.sin(angle);

                child.layout((int) x - child.getMeasuredWidth() / 2,
                        (int) y - child.getMeasuredHeight() / 2,
                        (int) x + child.getMeasuredWidth() / 2,
                        (int) y + child.getMeasuredHeight() / 2);

                mItemPositions.add(new ItemPosition(child, x, y, ViewUtils.radius(child)));
            }
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getVisibility() != View.GONE) {
            ViewFab fab = (ViewFab) v;

            for (ViewFabMenuListener listener: mListeners) {
                listener.onFabMenuButtonClicked(fab);
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        final float x = event.getX(), y = event.getY();

        if (!mCircle.contains(x, y)) {
            return false;
        }

        if (!isEnabled()) {
            return false;
        }

        if (isAnimating) {
            return false;
        }

        if (!isVisible()) {
            getParent().requestDisallowInterceptTouchEvent(true);
            View v = getClickedItem(x, y);
            if (v != null && v.getId() == R.id.menu_toggle) {
                toggleMenu();
            }
            return false;
        }

        mGestureDetector.onTouchEvent(event);

        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                if (y >= getHeight() - getResources().getDimension(R.dimen.status_bar_height)) {
                    return false;
                }

                mClickedItem = getClickedItem(x, y);

                if (isFlinging) {
                    isFlinging = false;
                }

                mLastAngle = mCircle.angleInDegrees(x, y);

                mStartY = y;
                break;
            case MotionEvent.ACTION_MOVE:
                double degrees = mCircle.angleInDegrees(x, y);
                double rotater = degrees - mLastAngle;

                if (isDragging) {
                    updateItemPositions(rotater);
                }

                mLastAngle = degrees;

                isDragging = true;
                break;
            case MotionEvent.ACTION_UP:
                isDragging = false;
                if (mClickedItem != null && mClickedItem == getClickedItem(x, y)) {
                    onClick(mClickedItem);
                    mClickedItem = null;
                }
                break;
        }

        return true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawCircle(getCenterX(), getCenterY(), radius, mPaint);
    }

    public void addListener(ViewFabMenuListener listener) {
        mListeners.add(listener);
    }

    private void rotateToggleOpen() {
        ObjectAnimator.ofFloat(toggle, View.ROTATION,
                toggle.getRotation(), toggle.getRotation() - 135f).start();
    }

    private void rotateToggleClosed() {
        ObjectAnimator.ofFloat(toggle, View.ROTATION,
                toggle.getRotation(), toggle.getRotation() - 135f)
                .setDuration(HIDE_DIFF + DURATION + DELAY_INCREMENT * buttonsList.size())
                .start();
    }

    private void updateItemPositions(double rotater) {
        for (ItemPosition itemPosition: mItemPositions) {
            itemPosition.update(rotater);
        }
    }

    private ViewFab getClickedItem(float x, float y) {
        for (ItemPosition position: mItemPositions) {
            if (position.contains(x, y)) {
                return position.mView;
            }
        }

        return null;
    }

    public void toggleMenu() {
        if (!isAnimating) {
            if (isMenuShowing) {
                hide();
            } else {
                show();
            }
        }
    }

    private void show() {
        if (!isMenuShowing && !isAnimating) {
            rotateToggleOpen();

            ArrayList<Animator> anims = new ArrayList<>();

            ObjectAnimator background = ObjectAnimator.ofFloat(this, RADIUS, mMaxRadius);
            background.setDuration(DURATION + DELAY_INCREMENT * buttonsList.size());
            background.setInterpolator(OVERSHOOT_INTERPOLATOR);

            anims.add(background);

            ItemPosition max = Collections.max(mItemPositions, new ItemPositionComparator());
            int ndxOfMax = mItemPositions.indexOf(max);
            int delay = INITIAL_DELAY;
            for (int i = 0; i < mItemPositions.size(); i++) {
                int sum = i + ndxOfMax;
                if (sum > mItemPositions.size() - 1) {
                    sum -= mItemPositions.size();
                }

                ItemPosition position = mItemPositions.get(sum);
                View view = position.mView;

                if (view.getId() == R.id.menu_toggle) {
                    continue;
                }

                float diffX = position.mItemCircle.getCenterX() - getCenterX();
                float diffY = position.mItemCircle.getCenterY() - getCenterY();

                ObjectAnimator out = ObjectAnimator.ofPropertyValuesHolder(view,
                        PropertyValuesHolder.ofFloat(View.X, view.getX() + diffX),
                        PropertyValuesHolder.ofFloat(View.Y, view.getY() + diffY),
                        PropertyValuesHolder.ofFloat(View.ALPHA, 0.0f, 1.0f));
                out.setStartDelay(delay);
                out.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        view.setVisibility(View.VISIBLE);
                    }
                });
                out.setDuration(DURATION);
                out.setInterpolator(OVERSHOOT_INTERPOLATOR);
                delay += DELAY_INCREMENT;

                anims.add(out);
            }

            AnimatorSet set = new AnimatorSet();
            set.playTogether(anims);
            set.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {
                    isAnimating = true;
                    isMenuShowing = true;
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    isAnimating = false;
                }
            });
            set.start();
        }
    }

    private void hide() {
        if (isMenuShowing && !isAnimating) {
            rotateToggleClosed();

            ArrayList<Animator> anims = new ArrayList<>();

            ObjectAnimator background = ObjectAnimator.ofFloat(this, RADIUS, radius, 0f);
            background.setDuration(HIDE_DIFF + DURATION + DELAY_INCREMENT * buttonsList.size());
            background.setInterpolator(ANTICIPATE_INTERPOLATOR);

            anims.add(background);

            ItemPosition max = Collections.max(mItemPositions, new ItemPositionComparator());
            int ndxOfMax = mItemPositions.indexOf(max);
            int delay = INITIAL_DELAY;
            for (int i = 0; i < mItemPositions.size(); i++) {
                int sum = i + ndxOfMax;
                if (sum > mItemPositions.size() - 1) {
                    sum -= mItemPositions.size();
                }

                ItemPosition position = mItemPositions.get(sum);
                View view = position.mView;

                if (view.getId() == R.id.menu_toggle) {
                    continue;
                }

                float diffX = position.mItemCircle.getCenterX() - getCenterX();
                float diffY = position.mItemCircle.getCenterY() - getCenterY();

                ObjectAnimator out = ObjectAnimator.ofPropertyValuesHolder(view,
                        PropertyValuesHolder.ofFloat(View.X, view.getX() - diffX),
                        PropertyValuesHolder.ofFloat(View.Y, view.getY() - diffY),
                        PropertyValuesHolder.ofFloat(View.ALPHA, 1.0f, 0.0f));
                out.setStartDelay(delay);
                out.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        view.setVisibility(View.GONE);
                    }
                });
                out.setDuration(DURATION);
                out.setInterpolator(ANTICIPATE_INTERPOLATOR);
                delay += DELAY_INCREMENT;

                anims.add(out);
            }

            AnimatorSet set = new AnimatorSet();
            set.playTogether(anims);
            set.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {
                    isAnimating = true;
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    isMenuShowing = false;
                    isAnimating = false;
                }
            });
            set.start();
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
        saver.stopSaveAnimation();
    }

    public void onEvent(EventFilenameChosen eventFilenameChosen) {
        if (NetworkUtils.hasInternet(getContext())) {
            saver.startSaveAnimation();
        }
    }

    public boolean isVisible() {
        return isMenuShowing && getVisibility() == View.VISIBLE;
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

    public float getCircleRadius() { return mCircle.getRadius(); }

    private final class ItemPosition {

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
            return mItemCircle.getBoundingRect().contains(x, y);
        }
    }

    private final class ItemPositionComparator implements Comparator<ItemPosition> {

        @Override
        public int compare(ItemPosition lhs, ItemPosition rhs) {
            Circle left = lhs.mItemCircle;
            Circle right = rhs.mItemCircle;
            if (left.getCenterX() < right.getCenterX()) return -1;
            if (left.getCenterX() > right.getCenterX()) return 1;
            return 0;
        }
    }

    private final class GestureListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            if (isVisible() && !startedFromBottom(e1) && !startedFromBottom(e2)) {
                isDragging = false;

                isFlinging = true;

                double angle = mCircle.angleInDegrees(e2.getX() - e1.getX(), e2.getY() - e1.getY());
                float velocity = velocityX / 10 + velocityY / 10;
                post(new FlingRunnable(velocity, angle <= 45d));

                return true;
            }

            return false;
        }

        private boolean startedFromBottom(MotionEvent event) {
            return event.getY() >= getHeight() - getResources().getDimension(R.dimen.status_bar_height);
        }
    }

    private final class FlingRunnable implements Runnable {

        private float velocity;
        private boolean isRtL;

        public FlingRunnable(float velocity, boolean isRtL) {
            this.velocity = velocity;
            this.isRtL = isRtL;
        }

        @Override
        public void run() {
            if (Math.abs(velocity) > 5 && isFlinging) {
                if (isRtL && velocity > 0) {
                    updateItemPositions(-velocity / 75);
                } else {
                    updateItemPositions(velocity / 75);
                }

                velocity /= 1.0666F;

                post(this);
            } else {
                isFlinging = false;
            }
        }
    }

    private final static ViewUtils.FloatProperty<ViewFabMenu> RADIUS =
            new ViewUtils.FloatProperty<ViewFabMenu>("radius") {
        @Override
        public void setValue(ViewFabMenu object, float value) {
            object.setRadius(value);
        }

        @Override
        public Float get(ViewFabMenu object) {
            return object.getRadius();
        }
    };
}