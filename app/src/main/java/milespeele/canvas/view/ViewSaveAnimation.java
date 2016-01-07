package milespeele.canvas.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AnticipateOvershootInterpolator;
import android.view.animation.DecelerateInterpolator;

import java.util.ArrayList;
import java.util.Arrays;

import milespeele.canvas.R;
import milespeele.canvas.util.Logg;
import milespeele.canvas.util.ViewUtils;

/**
 * Created by mbpeele on 1/6/16.
 */
public class ViewSaveAnimation extends View {

    private Paint mPaint;
    private AnimatorSet mAnimatorSet;
    private Drawable mDrawable;

    private int mBackgroundColor;
    private float mStart, mEnd;
    private float[] mAnimatedEnds;

    public ViewSaveAnimation(Context context) {
        super(context);
        init();
    }

    public ViewSaveAnimation(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ViewSaveAnimation(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public ViewSaveAnimation(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        mBackgroundColor = Color.WHITE;
        mPaint = new Paint();
        mPaint.setColor(Color.BLACK);

        mAnimatedEnds = new float[3];

        setWillNotDraw(false);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mStart = w * .1f;
        mEnd = w * .9f;
        Arrays.fill(mAnimatedEnds, mStart);

        mPaint.setStrokeWidth(20f);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        Rect bounds = canvas.getClipBounds();
        int prevColor = mPaint.getColor();

        mPaint.setColor(mBackgroundColor);
        canvas.drawRect(bounds, mPaint);

        if (mDrawable != null) {
            canvas.save();
            canvas.scale(getScaleX(), getScaleY(),
                    canvas.getWidth() / 2f, canvas.getHeight() / 2f);
            mDrawable.draw(canvas);
            canvas.restore();
        } else {
            mPaint.setColor(prevColor);
            int height = canvas.getHeight();
            canvas.drawLine(mStart, height * .25f, mAnimatedEnds[0], height * .25f, mPaint);
            canvas.drawLine(mStart, height * .5f, mAnimatedEnds[1], height * .5f, mPaint);
            canvas.drawLine(mStart, height * .75f, mAnimatedEnds[2], height * .75f, mPaint);
        }
    }

    public void setColors(int backgroundOfCanvas) {
        mBackgroundColor = ViewUtils.getComplimentColor(backgroundOfCanvas);

        mPaint.setColor(ViewUtils.getComplimentColor(mBackgroundColor));
    }

    public void startAnimation() {
        ArrayList<Animator> animators = new ArrayList<>();
        for (int i = 0; i < 3; i ++) {
            Animator animator = createLineAnimation(i);
            animator.setStartDelay(100 * i);
            animators.add(animator);
        }

        mAnimatorSet = new AnimatorSet();
        mAnimatorSet.playTogether(animators);
        mAnimatorSet.start();
    }

    public void stopAnimation(AnimatorListenerAdapter adapter) {
        ArrayList<Animator> childAnimations = mAnimatorSet.getChildAnimations();
        for (Animator animator: childAnimations) {
            if (animator instanceof ValueAnimator) {
                animator.removeAllListeners();
                ((ValueAnimator) animator).setRepeatCount(0);
                ((ValueAnimator) animator).setRepeatMode(0);
            }

            if (animator == childAnimations.get(childAnimations.size()  - 1)) {
                ObjectAnimator scale = ObjectAnimator.ofPropertyValuesHolder(this,
                        PropertyValuesHolder.ofFloat(View.SCALE_X, .2f, 1f),
                        PropertyValuesHolder.ofFloat(View.SCALE_Y, .2f, 1f));
                scale.setDuration(350);
                scale.setInterpolator(new AnticipateOvershootInterpolator());
                scale.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        super.onAnimationStart(animation);
                        mDrawable = getResources().getDrawable(R.drawable.ic_check_24dp);
                        mDrawable.setColorFilter(ViewUtils.getComplimentColor(mBackgroundColor),
                                PorterDuff.Mode.SRC_ATOP);
                        mDrawable.setBounds(0, 0, getWidth(), getHeight());
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        new Handler().postDelayed(() -> adapter.onAnimationEnd(animation), 750);
                    }
                });

                scale.addUpdateListener(animation -> invalidate());

                animator.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
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
        line.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mAnimatedEnds[index] = mStart;
            }
        });
        line.setRepeatCount(ValueAnimator.INFINITE);
        line.setRepeatMode(ValueAnimator.REVERSE);
        return line;
    }
}
