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

import milespeele.canvas.R;
import milespeele.canvas.util.ViewUtils;

/**
 * Created by mbpeele on 1/6/16.
 */
public class ViewSaveAnimator extends View {

    private Paint mPaint;
    private AnimatorSet mAnimatorSet;
    private Drawable mDrawable;
    private Path mPath;

    private int mBackgroundColor;
    private float mStart, mEnd;
    private float[] mAnimatedEnds;
    private boolean mShouldDrawLines = true;

    public ViewSaveAnimator(Context context) {
        super(context);
        init();
    }

    public ViewSaveAnimator(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ViewSaveAnimator(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public ViewSaveAnimator(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        mPath = new Path();

        mBackgroundColor = Color.BLACK;
        mPaint = new Paint();
        mPaint.setColor(Color.WHITE);

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

        mDrawable = getResources().getDrawable(R.drawable.ic_check_24dp);
        mDrawable.setColorFilter(ViewUtils.complementColor(mBackgroundColor),
                PorterDuff.Mode.SRC_ATOP);
        mDrawable.setBounds(0, 0, w, h);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        Rect bounds = canvas.getClipBounds();
        int prevColor = mPaint.getColor();

        mPath.rewind();
        mPath.addRoundRect(bounds.left, bounds.top, bounds.right, bounds.bottom,
                canvas.getWidth() * .1f, canvas.getHeight() * .1f, Path.Direction.CW);
        canvas.clipPath(mPath);

        mPaint.setColor(mBackgroundColor);
        canvas.drawRect(bounds, mPaint);

        if (!mShouldDrawLines) {
            canvas.save();
            float sx = getScaleX(), sy = getScaleY();
            float px = canvas.getWidth() / 2f, py = canvas.getHeight() / 2f;
            canvas.scale(sx, sy, px, py);
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
        mBackgroundColor = ViewUtils.complementColor(backgroundOfCanvas);

        mPaint.setColor(ViewUtils.complementColor(mBackgroundColor));
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
        mAnimatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                mShouldDrawLines = true;
            }
        });
        mAnimatorSet.start();
    }

    public void stopAnimation(AnimatorListenerAdapter adapter) {
<<<<<<< HEAD
        if (mAnimatorSet != null) {
            ArrayList<Animator> childAnimations = mAnimatorSet.getChildAnimations();
            Animator animator = childAnimations.get(childAnimations.size() - 1);
            animator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationRepeat(Animator animation) {
                    super.onAnimationEnd(animation);
                    animation.end();

                    for (Animator child: childAnimations) {
                        if (child instanceof ValueAnimator) {
                            ((ValueAnimator) child).setRepeatCount(0);
                            ((ValueAnimator) child).setRepeatMode(0);
                            child.removeAllListeners();
                            child.end();
                        }
=======
        ArrayList<Animator> childAnimations = mAnimatorSet.getChildAnimations();
        Animator animator = childAnimations.get(childAnimations.size() - 1);
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationRepeat(Animator animation) {
                super.onAnimationEnd(animation);
                for (Animator child: childAnimations) {
                    if (child instanceof ValueAnimator) {
                        ((ValueAnimator) child).setRepeatCount(0);
                        ((ValueAnimator) child).setRepeatMode(0);
                        child.removeAllListeners();
>>>>>>> Realm
                    }
                }

<<<<<<< HEAD
                    scaleAndFinish(adapter);
                }
            });
        } else {
            adapter.onAnimationEnd(null);
        }
=======
                ObjectAnimator scale = ObjectAnimator.ofPropertyValuesHolder(ViewSaveAnimator.this,
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
                        if (adapter != null) {
                            Arrays.fill(mAnimatedEnds, mStart);
                            new Handler().postDelayed(() -> adapter.onAnimationEnd(animation), 750);
                        }
                    }
                });

                scale.addUpdateListener(animation1 -> invalidate());
                scale.start();
            }
        });
>>>>>>> Realm
    }

    private void scaleAndFinish(AnimatorListenerAdapter adapter) {
        ObjectAnimator scale = ObjectAnimator.ofPropertyValuesHolder(this,
                PropertyValuesHolder.ofFloat(View.SCALE_X, .2f, 1f),
                PropertyValuesHolder.ofFloat(View.SCALE_Y, .2f, 1f));
        scale.setDuration(350);
        scale.setInterpolator(new AnticipateOvershootInterpolator());
        scale.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                mDrawable = ContextCompat.getDrawable(getContext(), R.drawable.ic_check_24dp);
                mDrawable.setColorFilter(ViewUtils.complementColor(mBackgroundColor),
                        PorterDuff.Mode.SRC_ATOP);
                mDrawable.setBounds(0, 0, getWidth(), getHeight());
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                new Handler().postDelayed(() -> adapter.onAnimationEnd(animation), 750);
            }
        });

        scale.addUpdateListener(animation1 -> invalidate());

        scale.start();
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
