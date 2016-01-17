package miles.scribble.ui.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AnticipateOvershootInterpolator;
import android.view.animation.DecelerateInterpolator;

import java.util.ArrayList;
import java.util.Arrays;

import miles.scribble.R;
import miles.scribble.util.Logg;
import miles.scribble.util.ViewUtils;

/**
 * Created by mbpeele on 1/6/16.
 */
public class LoadingAnimator extends View {

    private Paint mPaint;
    private AnimatorSet mAnimatorSet;
    private Drawable mDrawable;
    private Path mPath;

    private int mBackgroundColor, mStrokeColor;
    private float mStart, mEnd;
    private float[] mAnimatedEnds;
    private boolean mShouldDrawLines = true;

    public LoadingAnimator(Context context) {
        super(context);
        init();
    }

    public LoadingAnimator(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public LoadingAnimator(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public LoadingAnimator(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        mPath = new Path();

        mBackgroundColor = ContextCompat.getColor(getContext(), R.color.primary);
        mStrokeColor = ContextCompat.getColor(getContext(), R.color.primary_light);
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(mStrokeColor);

        mAnimatedEnds = new float[3];

        setWillNotDraw(false);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mStart = w * .15f;
        mEnd = w * .85f;
        Arrays.fill(mAnimatedEnds, mStart);

        mPaint.setStrokeWidth(20f);

        mDrawable = ContextCompat.getDrawable(getContext(), R.drawable.ic_check_24dp);
        mDrawable.mutate();
        mDrawable.setColorFilter(mPaint.getColor(), PorterDuff.Mode.SRC_ATOP);
        mDrawable.setBounds(0, 0, w, h);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        Rect bounds = canvas.getClipBounds();

        mPath.rewind();
        mPath.addRoundRect(bounds.left, bounds.top, bounds.right, bounds.bottom,
                canvas.getWidth() * .1f, canvas.getHeight() * .1f, Path.Direction.CW);
        canvas.clipPath(mPath);

        mPaint.setColor(mBackgroundColor);
        canvas.drawRect(bounds, mPaint);

        if (!mShouldDrawLines) {
            mDrawable.draw(canvas);
            float sx = getScaleX(), sy = getScaleY();
            float px = canvas.getWidth() / 2f, py = canvas.getHeight() / 2f;
            canvas.scale(sx, sy, px, py);
        } else {
            mPaint.setColor(mStrokeColor);
            int height = canvas.getHeight();
            canvas.drawLine(mStart, height * .25f, mAnimatedEnds[0], height * .25f, mPaint);
            canvas.drawLine(mStart, height * .5f, mAnimatedEnds[1], height * .5f, mPaint);
            canvas.drawLine(mStart, height * .75f, mAnimatedEnds[2], height * .75f, mPaint);
        }
    }

    public void startAnimation() {
        if (getVisibility() == View.GONE) {
            ViewUtils.visible(this, 50);

            animate()
                    .scaleY(1f)
                    .scaleX(1f)
                    .setDuration(50);
        }

        if (mAnimatorSet == null) {
            ArrayList<Animator> animators = new ArrayList<>();
            for (int i = 0; i < 3; i ++) {
                Animator animator = createLineAnimation(i);
                animator.setStartDelay(100 * i);
                animators.add(animator);
            }

            mAnimatorSet = new AnimatorSet();
            mAnimatorSet.playTogether(animators);
            mAnimatorSet.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {
                    mShouldDrawLines = true;
                }
            });
            mAnimatorSet.start();
        }
    }

    public void stopAnimation(AnimatorListenerAdapter adapter, int delay) {
        ArrayList<Animator> childAnimations = mAnimatorSet.getChildAnimations();
        for (Animator child: childAnimations) {
            ValueAnimator valueAnimator = (ValueAnimator) child;
            valueAnimator.setRepeatCount(0);
            valueAnimator.setRepeatMode(0);

            if (child == childAnimations.get(childAnimations.size() - 1)) {
                child.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        ObjectAnimator scale = ObjectAnimator.ofPropertyValuesHolder(
                                LoadingAnimator.this,
                                PropertyValuesHolder.ofFloat(View.SCALE_X, .2f, 1f),
                                PropertyValuesHolder.ofFloat(View.SCALE_Y, .2f, 1f));
                        scale.setDuration(350);
                        scale.setInterpolator(new AnticipateOvershootInterpolator());
                        scale.addListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationStart(Animator animation) {
                                mShouldDrawLines = false;
                            }

                            @Override
                            public void onAnimationEnd(Animator animation) {
                                Arrays.fill(mAnimatedEnds, mStart);
                                mAnimatorSet = null;
                                ViewUtils.gone(LoadingAnimator.this, delay);
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        adapter.onAnimationEnd(animation);
                                    }
                                }, delay);
                            }
                        });
                        scale.start();
                    }
                });
            }
        }
    }

    private Animator createLineAnimation(int index) {
        ValueAnimator line = ValueAnimator.ofFloat(mStart, mEnd);
        line.setDuration(1000);
        line.setInterpolator(new DecelerateInterpolator());
        line.addUpdateListener(animation -> {
            mAnimatedEnds[index] = (Float) animation.getAnimatedValue();
            invalidate();
        });
        line.setRepeatCount(ValueAnimator.INFINITE);
        line.setRepeatMode(ValueAnimator.REVERSE);
        return line;
    }
}
