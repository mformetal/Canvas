package milespeele.canvas.view;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.CornerPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathEffect;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Region;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v4.view.MotionEventCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.BounceInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;

import com.squareup.picasso.Cache;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import javax.inject.Inject;

import de.greenrobot.event.EventBus;
import milespeele.canvas.MainApp;
import milespeele.canvas.R;
import milespeele.canvas.event.EventDashboardButtonClicked;
import milespeele.canvas.paint.PaintStyles;
import milespeele.canvas.util.AbstractAnimatorListener;
import milespeele.canvas.util.FontUtils;
import milespeele.canvas.util.Logg;

/**
 * Created by mbpeele on 10/22/15.
 */
public class ViewDashboardButton extends ImageView implements Target {

    private final static int SCALE_DURATION = 150;
    private int width, height, baseLine;
    private int id;
    private int color;
    private String text;

    private Paint textPaint, rectPaint;
    private Bitmap circlified;
    private RectF rectF;
    private Path path;
    private AnimatorSet scale;
    private static final PathEffect corner = new CornerPathEffect(15);
    private static Interpolator INTERPOLATOR = new BounceInterpolator();

    @Inject Picasso picasso;
    @Inject EventBus bus;
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
        ((MainApp) getContext().getApplicationContext()).getApplicationComponent().inject(this);

        setLayerType(LAYER_TYPE_SOFTWARE, null);

        textPaint = PaintStyles.normal(Color.BLACK, 5f);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        textPaint.setTypeface(FontUtils.getStaticTypeFace(getContext(), "Roboto.ttf"));

        rectPaint = PaintStyles.normal(Color.WHITE, 5f);
        rectPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        rectPaint.setShadowLayer(12, 0, 0, Color.BLACK);

        path = new Path();

        rectF = new RectF();

        scale = new AnimatorSet();

        if (attrs != null) {
            TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.ViewDashboardButton);
            text = typedArray.getString(R.styleable.ViewDashboardButton_text);
            id = typedArray.getResourceId(R.styleable.ViewDashboardButton_image, R.drawable.ic_brush_black_24dp);
            color = typedArray.getColor(R.styleable.ViewDashboardButton_squareColor, Color.WHITE);
            rectPaint.setColor(color);
            typedArray.recycle();
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        width = w;
        height = h;

        float widthDiff = w * .45f;
        int centerX = width / 2, centerY = height / 2;
        rectF.left = centerX - widthDiff;
        rectF.right = centerX + widthDiff;
        rectF.top = centerY - widthDiff;
        rectF.bottom = centerY + widthDiff;

        adjustTextSize();
        adjustTextScale();

        circlified = cache.get(String.valueOf(id));
        if (circlified == null) {
            setVisibility(View.GONE);
            picasso.load(id)
                    .resize(Math.round(width * .7f), Math.round(width * .7f))
                    .into((Target) this);
        }

        path.moveTo(rectF.left, rectF.bottom * .7f);
        path.lineTo(rectF.left, rectF.top);
        path.lineTo(rectF.right, rectF.top);
        path.lineTo(rectF.right, rectF.bottom * .4f);
        path.close();

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
        int actionMasked = MotionEventCompat.getActionMasked(event);

        switch (actionMasked & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                if (rectF.contains(event.getX(), event.getY())) {
                    scaleUp();
                    textPaint.setColor(Color.WHITE);
                    invalidate();
                }
                break;


            case MotionEvent.ACTION_UP:
                if (rectF.contains(event.getX(), event.getY())) {
                    scaleDown();
                    textPaint.setColor(Color.BLACK);
                    invalidate();
                    bus.post(new EventDashboardButtonClicked(getId()));
                }
                break;
        }
        return true;
    }

    public static int darken(int color, double fraction) {
        int red = Color.red(color);
        int green = Color.green(color);
        int blue = Color.blue(color);
        red = darkenColor(red, fraction);
        green = darkenColor(green, fraction);
        blue = darkenColor(blue, fraction);
        int alpha = Color.alpha(color);

        return Color.argb(alpha, red, green, blue);
    }

    private static int darkenColor(int color, double fraction) {
        return (int)Math.max(color - (color * fraction), 0);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawRoundRect(rectF, 10, 10, rectPaint);
        rectPaint.setColor(darken(color, .3f));
        rectPaint.setPathEffect(corner);
        canvas.drawPath(path, rectPaint);
        rectPaint.setColor(color);
        rectPaint.setPathEffect(null);
        if (circlified != null) {
            canvas.drawBitmap(circlified, (width / 2) - circlified.getWidth() / 2,
                    (height / 2) - Math.round(circlified.getHeight() * .65), null);
        }
        canvas.drawText(text, rectF.centerX(), rectF.bottom - 20, textPaint);
    }

    private void adjustTextSize() {
        textPaint.setTextSize(100);
        textPaint.setTextScaleX(1.0f);
        Rect bounds = new Rect();
        // ask the paint for the bounding rect if it were to draw this
        // text
        textPaint.getTextBounds(text, 0, text.length(), bounds);
        // get the height that would have been produced
        int h = bounds.bottom - bounds.top;
        // make the text text up 70% of the height
        float target = (float) height * .1f;
        // figure out what textSize setting would create that height
        // of text
        float size = ((target/h) * 100f);
        // and set it into the paint
        textPaint.setTextSize(size);
    }

    private void adjustTextScale() {
        // do calculation with scale of 1.0 (no scale)
        textPaint.setTextScaleX(1.0f);
        Rect bounds = new Rect();
        // ask the paint for the bounding rect if it were to draw this
        // text.
        textPaint.getTextBounds(text, 0, text.length(), bounds);
        // determine the width
        int w = bounds.right - bounds.left;
        // calculate the baseline to use so that the
        // entire text is visible including the descenders
        int text = bounds.bottom - bounds.top;
        baseLine = bounds.bottom + ((height-text) / 2);
        // determine how much to scale the width to fit the view
        float xscale = ((float) (width - getPaddingLeft() - getPaddingRight())) / w;
        // set the scale for the text paint
        textPaint.setTextScaleX(xscale * .5f);
    }

    private void scaleUp() {
        scale.playTogether(ObjectAnimator.ofFloat(this, "scaleX", 1f, 1.05f),
                ObjectAnimator.ofFloat(this, "scaleY", 1f, 1.05f));
        scale.setInterpolator(INTERPOLATOR);
        scale.setDuration(SCALE_DURATION);
        scale.start();
    }

    private void scaleDown() {
        scale.playTogether(ObjectAnimator.ofFloat(this, "scaleX", 1f),
                ObjectAnimator.ofFloat(this, "scaleY", 1f));
        scale.setInterpolator(INTERPOLATOR);
        scale.setDuration(SCALE_DURATION);
        scale.start();
    }
}
