package milespeele.canvas.view;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.support.v4.view.MotionEventCompat;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;

import java.util.Calendar;

import milespeele.canvas.drawing.DrawingPoint;
import milespeele.canvas.util.Logg;
import milespeele.canvas.util.ViewUtils;

/**
 * Created by mbpeele on 11/11/15.
 */
public class ViewColorPicker extends View {

    private Paint colorSolidPaint;
    private Bitmap items;
    private GestureDetector detector;
    private Matrix rotateMatrix;

    private boolean allowRotation = true;
    private boolean[] quadrantTouched;
    private int width;
    private float translation;
    private float radius;
    private int curColor;
    private float rotater;
    private double startAngle;
    private long startClickTime;

    private final static long MAX_CLICK_DURATION = 200;
    private final static float CIRCLE_RADIUS = 25f;
    private final static float SHADOW_RADIUS = 30f;
    private static final int[] COLORS = ViewUtils.rainbow();
    private static final double ANGLE = Math.toRadians(360 / COLORS.length);

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
        rotateMatrix = new Matrix();

        quadrantTouched = new boolean[] { false, false, false, false, false };

        detector = new GestureDetector(getContext(), new FlingDetector());

        colorSolidPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        colorSolidPaint.setStyle(Paint.Style.FILL_AND_STROKE);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int defaultSize = getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec);

        setMeasuredDimension((width = defaultSize), defaultSize);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        width = w;

        radius = width / 4;

        translation = width / 2;

        items = Bitmap.createBitmap(w, w, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(items);
        drawWheelItems(canvas);
    }

    private void drawWheelItems(Canvas canvas) {
        float centerX = canvas.getWidth() / 2;
        float centerY = canvas.getHeight() / 2;

        for (int i = 0; i < COLORS.length; i++) {
            double theta = i * ANGLE;

            float dx = (float) (centerX + radius * Math.sin(theta));
            float dy = (float) (centerY + -radius * Math.cos(theta));

            colorSolidPaint.setColor(COLORS[i]);

            canvas.drawCircle(dx, dy, CIRCLE_RADIUS, colorSolidPaint);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        colorSolidPaint.setColor(curColor);
        canvas.drawCircle(canvas.getWidth() / 2, canvas.getHeight() / 2, CIRCLE_RADIUS * 4, colorSolidPaint);

        canvas.save();
        canvas.concat(rotateMatrix);
        canvas.drawBitmap(items, rotateMatrix, null);
        canvas.restore();
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
                long clickDuration = Calendar.getInstance().getTimeInMillis() - startClickTime;
                if (clickDuration >= MAX_CLICK_DURATION) {
                    onTouchMove(event);
                }
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
            case MotionEvent.ACTION_CANCEL:
                onTouchUp(event);
                break;
        }

        quadrantTouched[getQuadrant(event.getX() - (width / 2), width - event.getY() - (width / 2))] = true;
        invalidate();

        return true;
    }

    private void onTouchDown(MotionEvent event) {
        startClickTime = Calendar.getInstance().getTimeInMillis();

        for (int i = 0; i < quadrantTouched.length; i++) {
            quadrantTouched[i] = false;
        }

        allowRotation = false;
        rotater = (float) getAngle(event.getX(), event.getY());
    }

    private void onTouchMove(MotionEvent event) {
        double currentAngle = getAngle(event.getX(), event.getY());
        rotate((float) (startAngle - currentAngle));
        startAngle = currentAngle;
    }

    private void rotate(float degrees) {
        rotateMatrix.postRotate(degrees, width / 2, width / 2);
        rotater = degrees;
    }

    private void onTouchUp(MotionEvent event) {
        allowRotation = true;
    }

    private double getAngle(double xTouch, double yTouch) {
        double x = xTouch - (width / 2d);
        double y = width - yTouch - (width / 2d);

        switch (getQuadrant(x, y)) {
            case 1:
                return Math.asin(y / Math.hypot(x, y)) * 180 / Math.PI;
            case 2:
                return 180 - Math.asin(y / Math.hypot(x, y)) * 180 / Math.PI;
            case 3:
                return 180 + (-1 * Math.asin(y / Math.hypot(x, y)) * 180 / Math.PI);
            case 4:
                return 360 + Math.asin(y / Math.hypot(x, y)) * 180 / Math.PI;
            default:
                return 0;
        }
    }

    private static int getQuadrant(double x, double y) {
        if (x >= 0) {
            return y >= 0 ? 1 : 4;
        } else {
            return y >= 0 ? 2 : 3;
        }
    }

    public void setCurrentColor(int color) {
        curColor = color;
        colorSolidPaint.setColor(color);
        invalidate();
    }

    public void setColor(int color) {
        colorSolidPaint.setColor(color);
        invalidate();
        if (listener != null) {
            listener.onColorChanged(color);
        }
    }

    public void setListener(ViewColorPickerListener listener) {
        this.listener = listener;
    }

    private class FlingDetector extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            post(new FlingRunnable(velocityX + velocityY));

            int q1 = getQuadrant(e1.getX() - (width / 2), width - e1.getY() - (width / 2));
            int q2 = getQuadrant(e2.getX() - (width / 2), width - e2.getY() - (width / 2));

            if ((q1 == 2 && q2 == 2 && Math.abs(velocityX) < Math.abs(velocityY))
                    || (q1 == 3 && q2 == 3)
                    || (q1 == 1 && q2 == 3)
                    || (q1 == 4 && q2 == 4 && Math.abs(velocityX) > Math.abs(velocityY))
                    || ((q1 == 2 && q2 == 3) || (q1 == 3 && q2 == 2))
                    || ((q1 == 3 && q2 == 4) || (q1 == 4 && q2 == 3))
                    || (q1 == 2 && q2 == 4 && quadrantTouched[3])
                    || (q1 == 4 && q2 == 2 && quadrantTouched[3])) {

                post(new FlingRunnable(-1 * (velocityX + velocityY)));
            } else {
                post(new FlingRunnable(velocityX + velocityY));
            }

            return true;
        }
    }

    private class FlingRunnable implements Runnable {

        private float velocity;

        public FlingRunnable(float velocity) {
            this.velocity = velocity;
        }

        @Override
        public void run() {
            if (Math.abs(velocity) > 5 && allowRotation) {
                rotate(velocity / 75);

                velocity /= 1.0666F;

                post(this);
            }
        }
    }
}
