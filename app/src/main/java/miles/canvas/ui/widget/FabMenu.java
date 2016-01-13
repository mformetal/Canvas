package miles.canvas.ui.widget;

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
import android.view.animation.DecelerateInterpolator;
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
import miles.canvas.MainApp;
import miles.canvas.R;
import miles.canvas.data.event.EventBrushChosen;
import miles.canvas.data.event.EventColorChosen;
import miles.canvas.util.Circle;
import miles.canvas.util.ViewUtils;
import miles.canvas.ui.widget.Fab;

/**
 * Created by milespeele on 8/7/15.
 */
public class FabMenu extends ViewGroup implements View.OnClickListener {

    @Bind(R.id.menu_toggle) Fab toggle;
    @Bind(R.id.menu_erase) Fab eraser;
    @Bind(R.id.menu_upload) Fab saver;

    @Bind({R.id.menu_upload, R.id.menu_text, R.id.menu_stroke_color, R.id.menu_canvas_color,
            R.id.menu_ink, R.id.menu_brush, R.id.menu_undo, R.id.menu_redo, R.id.menu_erase,
            R.id.menu_image, R.id.menu_navigation})
    List<Fab> buttonsList;

    @Inject EventBus bus;

    private Circle mCircle;
    private Fab mClickedItem;
    private ArrayList<ItemPosition> mItemPositions;
    private GestureDetector mGestureDetector;
    private static final Interpolator OVERSHOOT_INTERPOLATOR = new OvershootInterpolator();
    private static final Interpolator ANTICIPATE_INTERPOLATOR = new AnticipateInterpolator();

    private boolean isMenuShowing = true;
    private boolean isAnimating = false;
    private boolean isDragging = false;
    private boolean isFlinging = false;
    private double mLastAngle;
    private float mStartY;
    private final static int INITIAL_DELAY = 0;
    private final static int DURATION = 400;
    private final static int DELAY_INCREMENT = 15;
    private final static int HIDE_DIFF = 50;

    private ArrayList<ViewFabMenuListener> mListeners;
    public interface ViewFabMenuListener {
        void onFabMenuButtonClicked(Fab v);
    }

    public FabMenu(Context context) {
        super(context);
        init();
    }

    public FabMenu(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public FabMenu(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public FabMenu(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        ((MainApp) getContext().getApplicationContext()).getApplicationComponent().inject(this);
        bus.register(this);

        mListeners = new ArrayList<>();

        mItemPositions = new ArrayList<>();

        mGestureDetector = new GestureDetector(getContext(), new GestureListener());

        setWillNotDraw(false);
        setDescendantFocusability(FOCUS_AFTER_DESCENDANTS);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        ButterKnife.bind(this);
        toggle.setRotation(45);
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

        mCircle = new Circle(ViewUtils.relativeCenterX(toggle), ViewUtils.relativeCenterY(toggle),
                toggle.getMeasuredHeight() * 3.75f);
        mItemPositions.add(new ItemPosition(toggle, getCenterX(), getCenterY(), ViewUtils.radius(toggle)));

        float mItemRadius = toggle.getMeasuredHeight() * 3;
        final int count = getChildCount();
        final double slice = Math.toRadians(360d / (count - 1));

        for (int i = 0; i < count; i++) {
            final Fab child = (Fab) getChildAt(i);
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

        hide();
    }

    @Override
    public void onClick(View v) {
        if (v.getVisibility() != View.GONE) {
            Fab fab = (Fab) v;

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
                if (isFromBottom(event)) {
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

    private boolean isSwipeFromBottom(MotionEvent event) {
        return event.getAction() == MotionEvent.ACTION_DOWN
                && event.getY() >= getHeight() - getResources().getDimension(R.dimen.status_bar_height);

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

    private Fab getClickedItem(float x, float y) {
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

                ObjectAnimator rotate = ObjectAnimator.ofFloat(view,
                        View.ROTATION, 0f, 360f);
                rotate.setInterpolator(new DecelerateInterpolator());
                rotate.setDuration(DURATION);
                rotate.setStartDelay(delay);

                delay += DELAY_INCREMENT;

                anims.add(out);
                anims.add(rotate);
            }

            AnimatorSet set = new AnimatorSet();
            set.playTogether(anims);
            set.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {
                    ((CanvasLayout) getParent()).showBackground();
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
                out.setStartDelay(delay);
                out.setDuration(DURATION);
                out.setInterpolator(ANTICIPATE_INTERPOLATOR);

                ObjectAnimator rotate = ObjectAnimator.ofFloat(view,
                        View.ROTATION, 0f, 360f);
                rotate.setInterpolator(new DecelerateInterpolator());
                rotate.setDuration(DURATION);
                rotate.setStartDelay(delay);

                delay += DELAY_INCREMENT;

                anims.add(out);
                anims.add(rotate);
            }

            AnimatorSet set = new AnimatorSet();
            set.playTogether(anims);
            set.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {
                    isAnimating = true;
                    ((CanvasLayout) getParent()).hideBackground();
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

    public boolean isVisible() {
        return isMenuShowing && getVisibility() == View.VISIBLE;
    }

    public float getCenterX() { return mCircle.getCenterX(); }

    public float getCenterY() { return mCircle.getCenterY(); }

    public float getCircleRadius() { return mCircle.getRadius(); }

    private boolean isFromBottom(MotionEvent event) {
        return event.getY() >= (float) toggle.getBottom() -
                getResources().getDimension(R.dimen.status_bar_height) / 2;
    }

    private final class ItemPosition {

        private Circle mItemCircle;
        private Fab mView;

        public ItemPosition(Fab child, double itemX, double itemY, float radius) {
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
               if (isVisible() && !isFromBottom(e1) && !isFromBottom(e2) &&
                    isMinXDist(e1, e2)) {
                isDragging = false;

                isFlinging = true;

                double angle = mCircle.angleInDegrees(e2.getX() - e1.getX(), e2.getY() - e1.getY());
                float velocity = velocityX / 10 + velocityY / 10;
                post(new FlingRunnable(velocity, angle <= 45d));

                return true;
            }

            return false;
        }

        private boolean isMinXDist(MotionEvent e1, MotionEvent e2) {
            return Math.abs(e1.getX() - e2.getX()) > 50;
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
}