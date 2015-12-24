package milespeele.canvas.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.support.design.widget.FloatingActionButton;
import android.util.AttributeSet;
import android.view.SoundEffectConstants;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

import milespeele.canvas.R;
import milespeele.canvas.util.Logg;
import milespeele.canvas.util.ViewUtils;

public class ViewFab extends FloatingActionButton {

    private String buttonText;
    private Paint mPaint;
    private RectF mClipBounds;

    private boolean isScaledUp = false;
    private boolean isScaling = false;
    private float mAngle;
    private final static float START_ANGLE = -90f;

    public ViewFab(Context context) {
        super(context);
        init(null);
    }

    public ViewFab(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public ViewFab(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeCap(Paint.Cap.BUTT);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setColor(Color.WHITE);

        mClipBounds = new RectF();

        if (attrs != null) {
            TypedArray typedArray = getResources().obtainAttributes(attrs, R.styleable.ViewFab);
            buttonText = typedArray.getString(R.styleable.ViewFab_text);
            typedArray.recycle();
        }

        startSaveAnimation();
    }

    @Override
    public boolean performClick() {
        boolean hasListener = super.performClick();
        if (!hasListener) {
            playSoundEffect(SoundEffectConstants.CLICK);
        }
        return hasListener;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mClipBounds.right == 0) {
            Rect bounds = canvas.getClipBounds();
            mClipBounds.left = bounds.left;
            mClipBounds.top = bounds.top;
            mClipBounds.right = bounds.right;
            mClipBounds.bottom = bounds.bottom;
        }
//        canvas.drawPath(mPath, mPaint);
        canvas.drawArc(mClipBounds, START_ANGLE, mAngle, false, mPaint);
    }

    public void toggleScaled() {
        if (!isScaling) {
            if (isScaledUp) {
                scaleDown();
            } else {
                scaleUp();
            }
        }
    }

    public boolean isScaledUp() { return isScaledUp; }

    public void scaleUp() {
        if (!isScaledUp) {
            ObjectAnimator scaleDown = ObjectAnimator.ofPropertyValuesHolder(this,
                    PropertyValuesHolder.ofFloat(View.SCALE_X, 1f),
                    PropertyValuesHolder.ofFloat(View.SCALE_Y, 1f))
                    .setDuration(350);
            scaleDown.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {
                    setBackgroundTintList(getResources().getColorStateList(R.color.primary_light));
                    isScaling = true;
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    isScaledUp = false;
                }
            });
            scaleDown.start();
        }
    }

    public void scaleDown() {
        if (isScaledUp) {
            ObjectAnimator scaleUp = ObjectAnimator.ofPropertyValuesHolder(this,
                    PropertyValuesHolder.ofFloat(View.SCALE_X, 1f, 1.1f),
                    PropertyValuesHolder.ofFloat(View.SCALE_Y, 1f, 1.1f))
                    .setDuration(350);
            scaleUp.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {
                    setBackgroundTintList(getResources( ).getColorStateList(R.color.accent));
                    isScaling = true;
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    isScaledUp = true;
                }
            });
            scaleUp.start();
        }
    }

    public void startSaveAnimation() {
        playPositiveAngleAnimation();
    }

    public void stopSaveAnimation() {
    }

    private void playPositiveAngleAnimation() {
        ObjectAnimator angle = ObjectAnimator.ofFloat(this, ANGLE, mAngle, 360f);
        angle.setInterpolator(new AccelerateDecelerateInterpolator());
        angle.setDuration(1000);
        angle.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                playNegativeAngleAnimation();
            }
        });
        angle.start();
    }

    private void playNegativeAngleAnimation() {
        mAngle = -360f;
        ObjectAnimator angle = ObjectAnimator.ofFloat(this, ANGLE, mAngle, 0);
        angle.setInterpolator(new AccelerateDecelerateInterpolator());
        angle.setDuration(1000);
        angle.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                playPositiveAngleAnimation();
            }
        });
        angle.start();
    }

    public String getButtonText() {
        return (buttonText != null) ? buttonText : "";
    }

    public float getAngle() {
        return mAngle;
    }

    public void setAngle(float angle) {
        mAngle = angle;
        invalidate();
    }

    private final static ViewUtils.FloatProperty<ViewFab> ANGLE =
            new ViewUtils.FloatProperty<ViewFab>("angle") {

        @Override
        public void setValue(ViewFab object, float value) {
            object.setAngle(value);
        }

        @Override
        public Float get(ViewFab object) {
            return object.getAngle();
        }
    };
}