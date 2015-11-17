package milespeele.canvas.view;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RadialGradient;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.SweepGradient;
import android.support.v4.view.MotionEventCompat;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.BounceInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.OvershootInterpolator;

import com.google.common.primitives.Ints;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import milespeele.canvas.R;
import milespeele.canvas.paint.PaintStyles;
import milespeele.canvas.util.Logg;
import milespeele.canvas.util.TextUtils;
import milespeele.canvas.util.ViewUtils;

/**
 * Created by mbpeele on 11/11/15.
 */
public class ViewColorPicker extends View {

    private Paint circlePaint;
    private Bitmap circleWheel;
    private final static Interpolator INTERPOLATOR = new AccelerateDecelerateInterpolator();

    private int width, height;
    private float animRadius;
    private float outerRadius;
    private int curColor, ndx = -1;

    private final static float OUTER_CIRCLE_RADIUS = 50f;
    private final static float INNER_CIRCLE_RADIUS = OUTER_CIRCLE_RADIUS * 4;
    private final static float STROKE_THICKNESS = 20f;
    private static final List<Integer> COLORS = Ints.asList(ViewUtils.rainbow());
    private static final double ANGLE = Math.toRadians(360 / COLORS.size());

    private ViewColorPickerListener listener;
    public interface ViewColorPickerListener {
        void onColorChanged(int newColor);
    }

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
        circlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        circlePaint.setStyle(Paint.Style.FILL_AND_STROKE);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        width = w;
        height = h;

        outerRadius = width / 3;

        circleWheel = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(circleWheel);
        drawWheelItems(canvas);
    }

    private void drawWheelItems(Canvas canvas) {
        float centerX = canvas.getWidth() / 2;
        float centerY = canvas.getHeight() / 2;

        for (int i = 0; i < COLORS.size(); i++) {
            double theta = i * ANGLE;

            float dx = (float) (centerX + outerRadius * Math.sin(theta));
            float dy = (float) (centerY - outerRadius * Math.cos(theta));

            circlePaint.setColor(COLORS.get(i));

            canvas.drawCircle(dx, dy, OUTER_CIRCLE_RADIUS, circlePaint);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (animRadius == 0) {
            canvas.drawCircle(canvas.getWidth() / 2, canvas.getHeight() / 2, INNER_CIRCLE_RADIUS, circlePaint);
        } else {
            canvas.drawCircle(canvas.getWidth() / 2, canvas.getHeight() / 2, animRadius, circlePaint);
        }

        canvas.drawBitmap(circleWheel, 0, 0, null);

        if (COLORS.contains(curColor)) {
            double theta = ndx * ANGLE;

            float dx = (float) (canvas.getWidth() / 2 + outerRadius * Math.sin(theta));
            float dy = (float) (canvas.getHeight() / 2 - outerRadius * Math.cos(theta));

            circlePaint.setStrokeWidth(STROKE_THICKNESS);
            circlePaint.setStyle(Paint.Style.STROKE);
            canvas.drawLine(canvas.getWidth() / 2, canvas.getHeight() / 2, dx, dy, circlePaint);
            circlePaint.setStyle(Paint.Style.FILL_AND_STROKE);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int actionMasked = MotionEventCompat.getActionMasked(event);

        switch (actionMasked & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_POINTER_DOWN:
                onTouchDown(event);
                break;

            case MotionEvent.ACTION_MOVE:
                onTouchMove(event);
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
            case MotionEvent.ACTION_CANCEL:
                onTouchUp(event);
                break;
        }

        invalidate();

        return true;
    }

    private void onTouchDown(MotionEvent event) {
        getColorBasedOnPosition(event);
    }

    private void onTouchMove(MotionEvent event) {
        getColorBasedOnPosition(event);
    }

    private void onTouchUp(MotionEvent event) {
    }

    private void getColorBasedOnPosition(MotionEvent event) {
        int x = Math.round(event.getX()), y = Math.round(event.getY());

        if (coordsInBitmap(x, y)) {
            int color = circleWheel.getPixel(x, y);
            if (COLORS.contains(color)) {
                invalidateCenterColorCircle(color);
            }
        }
    }

    private boolean coordsInBitmap(int x, int y) {
        return (x > 0 && x < circleWheel.getWidth() - 1) &&
                (y > 0 && y < circleWheel.getHeight() - 1);
    }

    private void invalidateCenterColorCircle(int color) {
        passColorToListener(color);

        curColor = color;
        circlePaint.setColor(color);

        ndx = COLORS.indexOf(curColor);

        ObjectAnimator bounce = ObjectAnimator.ofFloat(this, CIRCLE, 0, INNER_CIRCLE_RADIUS);
        bounce.setDuration(150);
        bounce.setInterpolator(INTERPOLATOR);
        bounce.start();

        int centerX = width / 2;
        int centerY = height / 2;
        int roundedRad = Math.round(INNER_CIRCLE_RADIUS);
        invalidate(centerX - roundedRad, centerY - roundedRad,
                centerX + roundedRad, centerY + roundedRad);
    }

    private void invalidateCenterColorCircle(float animRadius) {
        int centerX = width / 2;
        int centerY = height / 2;
        int roundedRad = Math.round(animRadius);
        invalidate(centerX - roundedRad, centerY - roundedRad,
                centerX + roundedRad, centerY + roundedRad);
    }

    private void passColorToListener(int color) {
        if (listener != null) {
            listener.onColorChanged(color);
        }
    }

    public void setCurrentColor(int color) {
        curColor = color;
        circlePaint.setColor(curColor);
        invalidate();
    }

    public void setListener(ViewColorPickerListener listener) {
        this.listener = listener;
    }

    public float getAnimRadius() {
        return animRadius;
    }

    public void setAnimRadius(float animRadius) {
        this.animRadius = animRadius;
        invalidateCenterColorCircle(animRadius);
    }

    private static  int[] listToIntArray() {
        int[] array = new int[COLORS.size()];
        for(int i = 0; i < COLORS.size(); i++)  {
            array[i] = COLORS.get(i);
        }
        return array;
    }

    private class FlingDetector extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            return true;
        }
    }

    private class FlingRunnable implements Runnable {

        public FlingRunnable() {
        }

        @Override
        public void run() {
        }
    }

    public static ViewUtils.FloatProperty<ViewColorPicker> CIRCLE = new ViewUtils.FloatProperty<ViewColorPicker>("circle") {
        @Override
        public void setValue(ViewColorPicker object, float value) {
            object.setAnimRadius(value);
        }

        @Override
        public Float get(ViewColorPicker object) {
            return object.getAnimRadius();
        }
    };
}
