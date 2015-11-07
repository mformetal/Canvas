package milespeele.canvas.view;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.CornerPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathEffect;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v4.view.MotionEventCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SoundEffectConstants;
import android.view.View;
import android.widget.ImageView;

import com.squareup.picasso.Cache;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import javax.inject.Inject;

import milespeele.canvas.MainApp;
import milespeele.canvas.R;
import milespeele.canvas.paint.PaintStyles;
import milespeele.canvas.util.AbstractAnimatorListener;
import milespeele.canvas.util.TextUtils;
import milespeele.canvas.util.ViewUtils;

/**
 * Created by mbpeele on 10/22/15.
 */
public class ViewDashboardButton extends ImageView implements Target {

    private final static int DURATION = 400;
    private int id;
    private int color, darkenedColor;
    private String text;
    private float lastTouchX, lastTouchY;
    private float radius;
    private boolean startRipple = false;

    private AnimatorSet ripple;
    private Paint textPaint, rectPaint, ripplePaint, shadowPaint;
    private Bitmap circlified;
    private RectF rectF, shadowRect;
    private Path path;
    private static final PathEffect CORNER = new CornerPathEffect(15);

    @Inject Picasso picasso;
    @Inject Cache cache;

    public ViewDashboardButton(Context context) {
        super(context);
        init(null);
    }

    public ViewDashboardButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public ViewDashboardButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public ViewDashboardButton(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        if (isInEditMode()) {
            return;
        }

        ((MainApp) getContext().getApplicationContext()).getApplicationComponent().inject(this);

        setLayerType(LAYER_TYPE_SOFTWARE, null);

        textPaint = PaintStyles.normal(Color.WHITE, 1f);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        textPaint.setTypeface(TextUtils.getStaticTypeFace(getContext(), "Roboto.ttf"));

        rectPaint = PaintStyles.normal(Color.WHITE, 5f);
        rectPaint.setStyle(Paint.Style.FILL_AND_STROKE);

        ripplePaint = PaintStyles.normal(Color.WHITE, 5f);
        ripplePaint.setStyle(Paint.Style.FILL_AND_STROKE);

        shadowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        shadowPaint.setColor(Color.BLACK);
        shadowPaint.setStyle(Paint.Style.FILL);
        shadowPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OVER));
        shadowPaint.setMaskFilter(new BlurMaskFilter(10f, BlurMaskFilter.Blur.NORMAL));

        path = new Path();

        rectF = new RectF();
        shadowRect = new RectF();

        if (attrs != null) {
            TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.ViewDashboardButton);
            text = typedArray.getString(R.styleable.ViewDashboardButton_text);
            id = typedArray.getResourceId(R.styleable.ViewDashboardButton_image, R.drawable.ic_brush_black_24dp);
            color = typedArray.getColor(R.styleable.ViewDashboardButton_squareColor, Color.WHITE);
            darkenedColor = ViewUtils.darken(color, .3f);
            rectPaint.setColor(color);
            ripplePaint.setColor(color);
            typedArray.recycle();
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        float widthDiff = w * .45f;
        int centerX = w / 2, centerY = h / 2;
        rectF.left = centerX - widthDiff;
        rectF.right = centerX + widthDiff;
        rectF.top = centerY - widthDiff;
        rectF.bottom = centerY + widthDiff;

        shadowRect.left = rectF.left - 5;
        shadowRect.right = rectF.right + 5;
        shadowRect.top = rectF.top - 5;
        shadowRect.bottom = rectF.bottom + 5;

        TextUtils.adjustTextSize(textPaint, text, h);
        TextUtils.adjustTextScale(textPaint, text, w, getPaddingLeft(), getPaddingRight());

        circlified = cache.get(String.valueOf(id));
        if (circlified == null) {
            setVisibility(View.GONE);
            picasso.load(id)
                    .resize(Math.round(w * .5f), Math.round(w * .5f))
                    .into((Target) this);
        }

        path.moveTo(rectF.left, rectF.bottom * .7f);
        path.lineTo(rectF.left, rectF.top);
        path.lineTo(rectF.right, rectF.top);
        path.lineTo(rectF.right, rectF.bottom * .4f);
        path.close();

        ripple = new AnimatorSet();

        ObjectAnimator circle = ObjectAnimator.ofFloat(this, "radius", 0, w / 4);
        circle.setDuration(DURATION);

        ObjectAnimator alpha =  ObjectAnimator.ofObject(ripplePaint, "alpha",
                new ArgbEvaluator(), ripplePaint.getAlpha(), 0);
        alpha.setDuration(DURATION);

        ripple.playTogether(circle, alpha);

        super.onSizeChanged(w, h, oldw, oldh);
    }

    @Override
    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
        circlified = bitmap;
        cache.set(String.valueOf(id), bitmap);

        postInvalidate();

        ObjectAnimator visibility = ObjectAnimator.ofFloat(this, "alpha", 0, 1);
        visibility.setDuration(300);
        visibility.addListener(new AbstractAnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationEnd(animation);
                setVisibility(View.VISIBLE);
            }
        });
        visibility.start();
    }

    @Override
    public void onBitmapFailed(Drawable errorDrawable) {

    }

    @Override
    public void onPrepareLoad(Drawable placeHolderDrawable) {

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX(), y = event.getY();
        int actionMasked = MotionEventCompat.getActionMasked(event);

        switch (actionMasked & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                if (rectF.contains(x, y)) {
                    lastTouchX = x;
                    lastTouchY = y;
                    playSoundEffect(SoundEffectConstants.CLICK);
                    showRipple();
                }
                break;
        }

        return false;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawRoundRect(rectF, 10, 10, rectPaint);
        rectPaint.setColor(darkenedColor);
        rectPaint.setPathEffect(CORNER);
        canvas.drawPath(path, rectPaint);
        rectPaint.setColor(color);
        rectPaint.setPathEffect(null);

        if (circlified != null) {
            canvas.drawBitmap(circlified, (getWidth() / 2) - circlified.getWidth() / 2,
                    (getHeight() / 2) - Math.round(circlified.getHeight() * .65), null);
        }

        canvas.drawText(text, rectF.centerX(), rectF.bottom - 30, textPaint);

        canvas.save();
        canvas.drawRoundRect(shadowRect, 10, 10, shadowPaint);
        canvas.restore();

        if (startRipple) {
            canvas.drawCircle(lastTouchX, lastTouchY, radius, ripplePaint);
            ripplePaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.XOR));
            canvas.drawRoundRect(rectF, 0, 0, ripplePaint);
            ripplePaint.setXfermode(null);
        }
    }

    public void showRipple() {
        if (rectF.contains(lastTouchX, lastTouchY)) {
            if (!ripple.isRunning()) {
                startRipple = true;

                ripple.addListener(new AbstractAnimatorListener() {

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        callOnClick();
                        ripplePaint.setAlpha(255);
                        radius = 0;
                        startRipple = false;
                    }
                });

                ripple.start();
            }
        }
    }

    public float getRadius() {
        return radius;
    }

    public void setRadius(float radius) {
        this.radius = radius;
        invalidate((int) (lastTouchX - radius),
                (int) (lastTouchY - radius),
                (int) (lastTouchX + radius),
                (int) (lastTouchY + radius));
    }
}