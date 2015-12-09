package milespeele.canvas.view;

import android.animation.Animator;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.FrameLayout;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import milespeele.canvas.R;
import milespeele.canvas.util.AbstractAnimatorListener;
import milespeele.canvas.util.Logg;
import milespeele.canvas.util.TextUtils;
import milespeele.canvas.util.ViewUtils;

/**
 * Created by mbpeele on 12/4/15.
 */
public class ViewColorPicker extends FrameLayout {

    @Bind(R.id.fragment_color_picker_pos_button) ViewTypefaceButton posButton;
    @Bind(R.id.fragment_color_picker_neg_button) ViewTypefaceButton negButton;
    @Bind(R.id.fragment_color_picker_switch) ViewTypefaceButton switcher;

    private final static int THUMB_SLIDE_DURATION = 350;
    private final static float START_X_MULT = .1f, END_X_MULT = .9f;
    private final static float LINE_THICKNESS = 15f;
    private float thumbRadius, circleRadius;
    private float slope, end, start;
    private float[] barY;
    private float redPos, bluePos, greenPos;
    private int curColor, curRed, curBlue, curGreen;

    private final static Interpolator INTERPOLATOR = new AccelerateDecelerateInterpolator();
    private ArrayList<Integer> colors;
    private Paint paint;
    private Bitmap slider;

    public ViewColorPicker(Context context) {
        super(context);
        init();
    }

    public ViewColorPicker(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ViewColorPicker(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public ViewColorPicker(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        paint = new Paint();
        paint.setStrokeWidth(LINE_THICKNESS);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setTypeface(TextUtils.getStaticTypeFace(getContext(), "Roboto.ttf"));

        setWillNotDraw(false);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = getDefaultSize(getMinimumWidth(), widthMeasureSpec);

        for (int i = 0; i < getChildCount(); i++) {
            measureChildWithMargins(getChildAt(i), widthMeasureSpec, 0, heightMeasureSpec, 0);
        }

        setMeasuredDimension(width, Math.round(ViewUtils.getScreenHeight(getContext()) * .8f));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawBitmap(slider, 0, 0, null);

        paint.setColor(curColor);
        canvas.drawRect(0,
                (colors.isEmpty()) ? 0 : circleRadius * 2.5f,
                canvas.getWidth(), canvas.getHeight() / 2, paint);

        paint.setColor(ViewUtils.getComplimentColor(curColor));
        String colorText = ViewUtils.colorToHexString(curColor);
        canvas.drawText(colorText, (canvas.getWidth() - paint.measureText(colorText)) / 2,
                (colors.isEmpty()) ? canvas.getHeight() * (5f / 16f) : canvas.getHeight() * (3f / 8f), paint);

        if (barY == null) {
            final float heightForBars = posButton.getTop() - canvas.getHeight() / 2;

            barY = new float[3];
            barY[0] = canvas.getHeight() / 2 + heightForBars * .25f;
            barY[1] = canvas.getHeight() / 2 + heightForBars * .5f;
            barY[2] = canvas.getHeight() / 2 + heightForBars * .75f;
        }

        paint.setColor(Color.RED);
        canvas.drawLine(canvas.getWidth() * START_X_MULT, barY[0], canvas.getWidth() * END_X_MULT, barY[0], paint);
        canvas.drawCircle(redPos, barY[0], thumbRadius, paint);

        paint.setColor(Color.GREEN);
        canvas.drawLine(canvas.getWidth() * START_X_MULT, barY[1], canvas.getWidth() * END_X_MULT, barY[1], paint);
        canvas.drawCircle(greenPos, barY[1], thumbRadius, paint);

        paint.setColor(Color.BLUE);
        canvas.drawLine(canvas.getWidth() * START_X_MULT, barY[2], canvas.getWidth() * END_X_MULT, barY[2], paint);
        canvas.drawCircle(bluePos, barY[2], thumbRadius, paint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();

        switch (action & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                onTouchDown(event);
                break;

            case MotionEvent.ACTION_MOVE:
                onTouchMove(event);
                break;

            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                onTouchUp(event);
                break;

            case MotionEvent.ACTION_POINTER_UP:
                break;
        }

        return true;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        ButterKnife.bind(this);
        switcher.setOnClickListener(switchClickListener);
    }

    public void initialize(int color, ArrayList<Integer> oldColors) {
        colors = oldColors;

        curColor = color;
        curRed = Color.red(curColor);
        curBlue = Color.blue(curColor);
        curGreen = Color.green(curColor);

        if (getWidth() == 0) {
            final ViewTreeObserver observer = getViewTreeObserver();
            observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    if (observer.isAlive()) {
                        observer.removeOnGlobalLayoutListener(this);
                    } else {
                        getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    }

                    int w = getWidth(), h = getHeight();

                    TextUtils.adjustTextScale(paint, "0xFFFFFF", w / 2, getPaddingLeft(), getPaddingRight());
                    TextUtils.adjustTextSize(paint, "0xFFFFFF", h / 2);

                    thumbRadius = (h * .9f) / 50;
                    circleRadius = w * .075f;

                    end = w * END_X_MULT;
                    start = w * START_X_MULT;
                    slope = (end - start) / 255f;

                    end = w * END_X_MULT;
                    start = w * START_X_MULT;
                    slope = (end - start) / 255f;

                    redPos = slope * (curRed) + start;
                    bluePos = slope * (curBlue) + start;
                    greenPos = slope * (curGreen) + start;

                    int width = Math.round(circleRadius * 1.25f + colors.size() * (2.5f * circleRadius));
                    int height = Math.round(circleRadius * 2.25f);
                    slider = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                    Canvas canvas = new Canvas(slider);
                    float x = circleRadius * 1.25f;
                    for (int old : colors) {
                        paint.setColor(old);
                        canvas.drawCircle(x, circleRadius * 1.25f, circleRadius, paint);
                        x += circleRadius * 2.5f;
                    }
                }
            });
        }
    }

    private void onTouchDown(MotionEvent event) {
        float x = event.getX(), y = event.getY();
        if (y <= slider.getHeight()) {
            getBitmapPixel(x, y);
        } else {
            slideSeekbar(x, y);
        }
    }

    private void onTouchMove(MotionEvent event) {
        float x = event.getX(), y = event.getY();
        if (y <= slider.getHeight()) {
        } else {
            slideSeekbar(x, y);
        }
    }

    private void onTouchUp(MotionEvent event) {
    }

    private void slideSeekbar(float x, float y) {
        for (int ndx = 0; ndx < barY.length; ndx++) {
            float linePos = barY[ndx];
            if (linePos - LINE_THICKNESS * 4 <= y && y <= linePos + LINE_THICKNESS * 4) {
                float value = (x - start) / slope;

                float pos;
                int color;

                if (x <= start) {
                    pos = start;
                    color = 0;
                } else if (x >= end) {
                    pos = end;
                    color = 255;
                } else {
                    pos = x;
                    color = Math.round(value);
                }

                switch (ndx) {
                    case 0:
                        redPos = pos;
                        curRed = color;
                        break;
                    case 1:
                        greenPos = pos;
                        curGreen = color;
                        break;
                    case 2:
                        bluePos = pos;
                        curBlue = color;
                        break;
                }

                curColor = Color.rgb(curRed, curGreen, curBlue);
                invalidate((int) start,
                        (int) (linePos - LINE_THICKNESS * 4),
                        (int) end,
                        (int) (linePos + LINE_THICKNESS * 4));
                break;
            }
        }
    }

    private void getBitmapPixel(float x, float y) {
        int newX = Math.round(x), newY = Math.round(y);
        int pixel = slider.getPixel(newX, newY);
        if (pixel != 0) {
            animateRectColor(pixel);

            curRed = Color.red(pixel);
            animateThumbPosition("RED", slope * (curRed) + start);

            curBlue = Color.blue(pixel);
            animateThumbPosition("BLUE", slope * (curBlue) + start);

            curGreen = Color.green(pixel);
            animateThumbPosition("GREEN", slope * (curGreen) + start);

            invalidate();
        }
    }

    private void animateRectColor(int newColor) {
        ValueAnimator rect = ObjectAnimator.ofObject(new ArgbEvaluator(), curColor, newColor);
        rect.addUpdateListener(animation -> {
            curColor = (Integer) animation.getAnimatedValue();
            invalidate(0,
                    (colors.isEmpty()) ? 0 : (int) (circleRadius * 2.5f),
                    getWidth(),
                    getHeight() / 2);
        });
        rect.addListener(new AbstractAnimatorListener() {
            @Override
            public void onAnimationEnd(Animator animation) {
                curColor = newColor;
            }
        });
        rect.start();
    }

    private void animateThumbPosition(String which, float newValue) {
        switch (which) {
            case "RED":
                ValueAnimator red = ObjectAnimator.ofFloat(redPos, newValue);
                red.setDuration(THUMB_SLIDE_DURATION);
                red.setInterpolator(INTERPOLATOR);
                red.addUpdateListener(animation -> {
                    redPos = (Float) animation.getAnimatedValue();
                    invalidate(
                            (int) (redPos - circleRadius),
                            (int) (redPos - circleRadius),
                            (int) (redPos + circleRadius),
                            (int) (redPos + circleRadius));
                });
                red.addListener(new AbstractAnimatorListener() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        redPos = newValue;
                    }
                });
                red.start();
                break;
            case "GREEN":
                ValueAnimator green = ObjectAnimator.ofFloat(greenPos, newValue);
                green.setDuration(THUMB_SLIDE_DURATION);
                green.setInterpolator(INTERPOLATOR);
                green.addUpdateListener(animation -> {
                    greenPos = (Float) animation.getAnimatedValue();
                    invalidate(
                            (int) (greenPos - circleRadius),
                            (int) (greenPos - circleRadius),
                            (int) (greenPos + circleRadius),
                            (int) (greenPos + circleRadius));
                });
                green.addListener(new AbstractAnimatorListener() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        greenPos = newValue;
                    }
                });
                green.start();
                break;
            case "BLUE":
                ValueAnimator blue = ObjectAnimator.ofFloat(greenPos, newValue);
                blue.setDuration(THUMB_SLIDE_DURATION);
                blue.setInterpolator(INTERPOLATOR);
                blue.addUpdateListener(animation -> {
                    bluePos = (Float) animation.getAnimatedValue();
                    invalidate(
                            (int) (bluePos - circleRadius),
                            (int) (bluePos - circleRadius),
                            (int) (bluePos + circleRadius),
                            (int) (bluePos + circleRadius));
                });
                blue.addListener(new AbstractAnimatorListener() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        bluePos = newValue;
                    }
                });
                blue.start();
                break;
        }
    }

    public int getSelectedColor() {
        return curColor;
    }

    private OnClickListener switchClickListener = v -> {
    };
}
