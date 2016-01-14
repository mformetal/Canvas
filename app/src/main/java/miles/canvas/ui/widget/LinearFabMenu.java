package miles.canvas.ui.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnticipateInterpolator;
import android.view.animation.DecelerateInterpolator;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import miles.canvas.R;
import miles.canvas.util.Circle;
import miles.canvas.util.Logg;
import miles.canvas.util.ViewUtils;

/**
 * Created by mbpeele on 1/13/16.
 */
public class LinearFabMenu extends ViewGroup implements View.OnClickListener {

    @Bind(R.id.activity_gallery_options_menu_toggle) Fab toggle;

    private final static int INITIAL_DELAY = 0;
    private final static int DURATION = 400;
    private final static int DELAY_INCREMENT = 15;
    private final static int HIDE_DIFF = 50;
    private boolean isAnimating = false;
    private boolean isMenuShowing = true;

    public LinearFabMenu(Context context) {
        super(context);
        init();
    }

    public LinearFabMenu(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public LinearFabMenu(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public LinearFabMenu(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
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
        int height = getDefaultSize(getSuggestedMinimumHeight(), heightMeasureSpec);

        setMeasuredDimension(width, height);
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

        toggle.layout(r - toggle.getMeasuredWidth() - lps.rightMargin,
                getMeasuredHeight() - toggle.getMeasuredHeight() - lps.bottomMargin,
                r - lps.rightMargin,
                getMeasuredHeight() - lps.bottomMargin);

        int cx = Math.round(ViewUtils.relativeCenterX(toggle));

        int curBottom = toggle.getTop() - lps.topMargin;

        for (int i = 0; i < getChildCount(); i++) {
            final Fab child = (Fab) getChildAt(i);
            if (child.getId() != R.id.activity_gallery_options_menu_toggle) {
                MarginLayoutParams params = (MarginLayoutParams) child.getLayoutParams();

                curBottom -= (params.bottomMargin);

                child.layout(cx - child.getMeasuredWidth() / 2,
                        curBottom - child.getMeasuredHeight(),
                        cx + child.getMeasuredWidth() / 2,
                        curBottom);

                child.setVisibility(View.GONE);

                curBottom -= child.getMeasuredHeight() + params.topMargin;
            }
        }

        hideMenu();
    }

    @Override
    @OnClick({R.id.activity_gallery_options_menu_toggle})
    public void onClick(View v) {
        toggleMenu();
    }

    private void rotateToggleOpen() {
        ObjectAnimator.ofFloat(toggle, View.ROTATION,
                toggle.getRotation(), toggle.getRotation() - 135f).start();
    }

    private void rotateToggleClosed() {
        ObjectAnimator.ofFloat(toggle, View.ROTATION,
                toggle.getRotation(), toggle.getRotation() - 135f)
                .setDuration(HIDE_DIFF + DURATION + DELAY_INCREMENT * getChildCount() - 1)
                .start();
    }

    public void toggleMenu() {
        if (!isAnimating) {
            if (isMenuShowing) {
                hideMenu();
            } else {
                showMenu();
            }
        }
    }

    public void showMenu() {
        if (!isMenuShowing && !isAnimating) {
            rotateToggleOpen();

            ArrayList<Animator> animators = new ArrayList<>();
            int delay = INITIAL_DELAY;
            for (int i = 0; i < getChildCount(); i++) {
                View view = getChildAt(i);
                if (view.getId() == R.id.activity_gallery_options_menu_toggle) {
                    continue;
                }

                ObjectAnimator gone = ViewUtils.visibleAnimator(view);
                gone.setStartDelay(delay);

                delay += DELAY_INCREMENT;

                animators.add(gone);
            }

            AnimatorSet set = new AnimatorSet();
            set.playTogether(animators);
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

    public void hideMenu() {
        if (isMenuShowing && !isAnimating) {
            rotateToggleClosed();

            ArrayList<Animator> animators = new ArrayList<>();
            int delay = INITIAL_DELAY;
            for (int i = 0; i < getChildCount(); i++) {
                View view = getChildAt(i);
                if (view.getId() == R.id.activity_gallery_options_menu_toggle) {
                    continue;
                }

                ObjectAnimator gone = ViewUtils.goneAnimator(view);
                gone.setStartDelay(delay);

                delay += DELAY_INCREMENT;

                animators.add(gone);
            }

            AnimatorSet set = new AnimatorSet();
            set.playTogether(animators);
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
}
