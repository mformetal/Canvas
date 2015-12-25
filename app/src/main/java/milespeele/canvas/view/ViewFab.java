package milespeele.canvas.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
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

    private String mText;
    private Paint mPaint;
    private RectF mClipBounds;
    private ObjectAnimator mPositiveAngleAnim, mNegativeAngleAnim;

    private boolean isScaledUp = false;
    private boolean isScaling = false;
    private float mAngle;
    private final static int ANGLE_DUR = 1000;
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
        mPaint.setStrokeWidth(3f);
        mPaint.setColor(Color.WHITE);

        mClipBounds = new RectF();

        if (attrs != null) {
            TypedArray typedArray = getResources().obtainAttributes(attrs, R.styleable.ViewFab);
            mText = typedArray.getString(R.styleable.ViewFab_text);
            typedArray.recycle();
        }
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
        canvas.drawArc(mClipBounds, START_ANGLE, mAngle, false, mPaint);
    }

    public void toggleScaled() {
        if (!isScaling) {
            if (isScaledUp) {
                Logg.log("DOWN");
                scaleDown();
            } else {
                Logg.log("UP");
                scaleUp();
            }
        }
    }

    public boolean isScaledUp() { return isScaledUp; }

    public void scaleDown() {
        ObjectAnimator scaleDown = ObjectAnimator.ofPropertyValuesHolder(this,
                PropertyValuesHolder.ofFloat(View.SCALE_X, 1f),
                PropertyValuesHolder.ofFloat(View.SCALE_Y, 1f))
                .setDuration(350);
        scaleDown.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                setBackgroundTintList(getResources().getColorStateList(R.color.accent));
                isScaling = true;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                isScaling = false;
                isScaledUp = false;
            }
        });
        scaleDown.start();
    }

    public void scaleUp() {
        ObjectAnimator scaleUp = ObjectAnimator.ofPropertyValuesHolder(this,
                PropertyValuesHolder.ofFloat(View.SCALE_X, 1f, 1.1f),
                PropertyValuesHolder.ofFloat(View.SCALE_Y, 1f, 1.1f))
                .setDuration(350);
        scaleUp.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                setBackgroundTintList(getResources( ).getColorStateList(R.color.primary_light));
                isScaling = true;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                isScaling = false;
                isScaledUp = true;
            }
        });
        scaleUp.start();
    }

    public void startSaveAnimation() {
        playPositiveAngleAnimation();
    }

    public void stopSaveAnimation() {
        if (mPositiveAngleAnim != null) {
            mPositiveAngleAnim.removeAllListeners();
        }

        if (mNegativeAngleAnim != null) {
            mNegativeAngleAnim.removeAllListeners();
        }

        mPositiveAngleAnim = null;
        mNegativeAngleAnim = null;
        mAngle = 0;
    }

    private void playPositiveAngleAnimation() {
        if (mPositiveAngleAnim == null) {
            mPositiveAngleAnim = ObjectAnimator.ofFloat(this, ANGLE, mAngle, 360f);
            mPositiveAngleAnim.setInterpolator(new AccelerateDecelerateInterpolator());
            mPositiveAngleAnim.setDuration(ANGLE_DUR);
            mPositiveAngleAnim.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    playNegativeAngleAnimation();
                }
            });
            mPositiveAngleAnim.start();
        } else {
            mPositiveAngleAnim.start();
        }
    }

    private void playNegativeAngleAnimation() {
        mAngle = -360f;
        if (mNegativeAngleAnim == null) {
            mNegativeAngleAnim = ObjectAnimator.ofFloat(this, ANGLE, mAngle, 0);
            mNegativeAngleAnim.setInterpolator(new AccelerateDecelerateInterpolator());
            mNegativeAngleAnim.setDuration(ANGLE_DUR);
            mNegativeAngleAnim.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    playPositiveAngleAnimation();
                }
            });
            mNegativeAngleAnim.start();
        } else {
            mNegativeAngleAnim.start();
        }
    }

    public String getText() {
        return (mText != null) ? mText : "";
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