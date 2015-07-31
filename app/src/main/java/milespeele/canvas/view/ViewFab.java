package milespeele.canvas.view;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.support.design.widget.FloatingActionButton;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import milespeele.canvas.R;
import milespeele.canvas.util.Util;

public class ViewFab extends FloatingActionButton {

    private AnimatorSet animateIn;
    private AnimatorSet animateOut;

    public static final int SIZE_NORMAL = 0;
    public static final int SIZE_MINI = 1;

    int mFabSize;
    boolean mShowShadow;
    int mShadowColor;
    int mShadowRadius = Util.dpToPx(getContext(), 4f);
    int mShadowXOffset = Util.dpToPx(getContext(), 1f);
    int mShadowYOffset = Util.dpToPx(getContext(), 3f);

    private boolean mUsingElevation;
    private boolean mUsingElevationCompat;

    public ViewFab(Context context) {
        super(context);
        init();
    }

    public ViewFab(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ViewFab(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        animateIn = new AnimatorSet();
        animateIn.playTogether(ObjectAnimator.ofFloat(this, "rotation", 0f, 360f),
                ObjectAnimator.ofFloat(this, "alpha", 0, 1));

        animateOut = new AnimatorSet();
        animateOut.playTogether(ObjectAnimator.ofFloat(this, "rotation", 0f, 360f),
                ObjectAnimator.ofFloat(this, "alpha", 1, 0));
    }

    private int getCircleSize() {
        return getResources().getDimensionPixelSize(mFabSize == SIZE_NORMAL
                ? R.dimen.fab_size_normal : R.dimen.fab_size_mini);
    }

    private int calculateMeasuredWidth() {
        return getCircleSize() + calculateShadowWidth();
    }

    private int calculateMeasuredHeight() {
        return getCircleSize() + calculateShadowHeight();
    }

    private int calculateShadowWidth() {
        return hasShadow() ? getShadowX() * 2 : 0;
    }

    int calculateShadowHeight() {
        return hasShadow() ? getShadowY() * 2 : 0;
    }

    private int getShadowX() {
        return mShadowRadius + Math.abs(mShadowXOffset);
    }

    private int getShadowY() {
        return mShadowRadius + Math.abs(mShadowYOffset);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(calculateMeasuredWidth(), calculateMeasuredHeight());
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void setLayoutParams(ViewGroup.LayoutParams params) {
        if (params instanceof ViewGroup.MarginLayoutParams && mUsingElevationCompat) {
            ((ViewGroup.MarginLayoutParams) params).leftMargin += getShadowX();
            ((ViewGroup.MarginLayoutParams) params).topMargin += getShadowY();
            ((ViewGroup.MarginLayoutParams) params).rightMargin += getShadowX();
            ((ViewGroup.MarginLayoutParams) params).bottomMargin += getShadowY();
        }
        super.setLayoutParams(params);
    }

    public boolean hasShadow() {
        return !mUsingElevation && mShowShadow;
    }

    @Override
    public void setElevation(float elevation) {
        if (Util.hasLollipop() && elevation > 0) {
            super.setElevation(elevation);
            if (!isInEditMode()) {
                mUsingElevation = true;
                mShowShadow = false;
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void setElevationCompat(float elevation) {
        mShadowColor = 0x26000000;
        mShadowRadius = Math.round(elevation / 2);
        mShadowXOffset = 0;
        mShadowYOffset = Math.round(mFabSize == SIZE_NORMAL ? elevation : elevation / 2);

        if (Util.hasLollipop()) {
            super.setElevation(elevation);
            mUsingElevationCompat = true;
            mShowShadow = false;

            ViewGroup.LayoutParams layoutParams = getLayoutParams();
            if (layoutParams != null) {
                setLayoutParams(layoutParams);
            }
        } else {
            mShowShadow = true;
        }
    }

    public void show() {
        animateIn.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        animateIn.start();
    }

    public void hide() {
        animateOut.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                setVisibility(View.GONE);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        animateOut.start();
    }

}