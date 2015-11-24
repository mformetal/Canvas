package milespeele.canvas.drawing;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.os.Handler;
import android.os.SystemClock;
import android.view.MotionEvent;

import java.util.ArrayList;
import java.util.Random;

import javax.inject.Inject;

import de.greenrobot.event.EventBus;
import milespeele.canvas.MainApp;
import milespeele.canvas.event.EventBrushChosen;
import milespeele.canvas.event.EventColorChosen;
import milespeele.canvas.paint.PaintStyles;
import milespeele.canvas.paint.PaintStore;
import milespeele.canvas.util.FileUtils;
import milespeele.canvas.util.Datastore;
import milespeele.canvas.util.Logg;
import milespeele.canvas.util.ViewUtils;

/**
 * Created by mbpeele on 9/25/15.
 */
public class DrawingCurve implements PaintStore.PaintStoreListener {

    public enum State {
        DRAW,
        ERASE,
        RAINBOW
    }

    private Bitmap mBitmap, cachedBitmap;
    private Canvas mCanvas;
    private DrawingPoints[] pointerPoints;
    private DrawingHistory allPoints, redoPoints;
    private PaintStore mPaint;
    private Random random;
    private State mState = State.DRAW;
    private ArrayList<Integer> colors;
    private Context mContext;

    private static final float VELOCITY_FILTER_WEIGHT = 0.2f;
    private static float STROKE_WIDTH = 10f;
    private static final float POINT_MAX_WIDTH = 50f, POINT_MIN_WIDTH = 2f, POINT_TOLERANCE = 5f;
    private int[] rainbow;
    private int currentStrokeColor, currentBackgroundColor;
    private int width, height;

    @Inject Datastore store;
    @Inject EventBus bus;

    public DrawingCurve(Context context, int w, int h) {
        ((MainApp) context.getApplicationContext()).getApplicationComponent().inject(this);
        bus.register(this);

        width = w;
        height = h;

        mContext = context;

        random = new Random();

        rainbow = ViewUtils.rainbow();

        colors = FileUtils.getColors(mContext);

        currentStrokeColor = Color.argb(255, random.nextInt(256), random.nextInt(256), random.nextInt(256));
        currentBackgroundColor = store.getLastBackgroundColor();

        cachedBitmap = FileUtils.getCachedBitmap(mContext);
        if (cachedBitmap == null) {
            cachedBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
            cachedBitmap.eraseColor(currentBackgroundColor);
        }
        mBitmap = cachedBitmap.copy(cachedBitmap.getConfig(), true);
        mCanvas = new Canvas(mBitmap);

        mPaint = new PaintStore(currentStrokeColor, STROKE_WIDTH);
        mPaint.setListener(this);

        allPoints = new DrawingHistory();
        redoPoints = new DrawingHistory();
        pointerPoints = new DrawingPoints[4];
        for (int ndx = 0; ndx < 4; ndx++) {
            pointerPoints[ndx] = new DrawingPoints(mPaint);
        }
    }

    @Override
    public void onColorChanged(int newColor) {
        colors.add(newColor);
    }

    public void changeState(State newValue) {
        mState = newValue;

        switch (mState) {
            case ERASE:
                mHandler.removeCallbacksAndMessages(null);
                mPaint.changePaint(PaintStyles.erase(currentBackgroundColor, 20f));
                break;
            case DRAW:
                mHandler.removeCallbacksAndMessages(null);
                mPaint.changePaint(PaintStyles.normal(currentStrokeColor, STROKE_WIDTH));
                break;
            case RAINBOW:
                paintRunnable.run();
                break;
        }
    }

    public void reset(int color) {
        for (ArrayList list: pointerPoints) {
            list.clear();
        }

        allPoints.clear();
        redoPoints.clear();

        mBitmap.recycle();
        mBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mBitmap);

        mCanvas.drawColor(color, PorterDuff.Mode.CLEAR);
    }

    public void onTouchUp(MotionEvent event) {
        final int pointer = (event.getAction() & MotionEvent.ACTION_POINTER_INDEX_MASK)
                >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;

        DrawingPoints pointsToClear = pointerPoints[pointer];
        allPoints.push(pointsToClear);
        pointsToClear.clear();
    }

    public void addPoint(float x, float y, int pointerId) {
        DrawingPoint prevPoint;
        DrawingPoint nextPoint = new DrawingPoint(x, y, SystemClock.currentThreadTimeMillis(), mPaint.getStrokeWidth());

        DrawingPoints points = pointerPoints[pointerId];
        if (points.isEmpty()) {
            mCanvas.drawPoint(x, y, mPaint);
            points.add(nextPoint);
        } else {
            prevPoint = points.getLast();

            if (Math.abs(prevPoint.x - x) < POINT_TOLERANCE && Math.abs(prevPoint.y - y) < POINT_TOLERANCE) {
                return;
            }

            switch (mState) {
                case ERASE:
                    mCanvas.drawLine(prevPoint.x, prevPoint.y, nextPoint.x, nextPoint.y, mPaint);
                    break;
                case DRAW:
                case RAINBOW:
                    points.add(nextPoint);
                    algorithmDraw(prevPoint, nextPoint, points);
            }
        }
    }

    private void algorithmDraw(DrawingPoint previous, DrawingPoint current, DrawingPoints points) {
        DrawingPoint mid = current.midPoint(previous);

        float velocity =  VELOCITY_FILTER_WEIGHT * current.velocityFrom(previous)
                + (1 - VELOCITY_FILTER_WEIGHT) * points.lastVelocity;
        float strokeWidth = Math.abs(STROKE_WIDTH - velocity);
        if (strokeWidth < POINT_MIN_WIDTH) { strokeWidth = POINT_MIN_WIDTH; }
        if (strokeWidth > POINT_MAX_WIDTH) { strokeWidth = POINT_MAX_WIDTH; }
        float diff = strokeWidth - points.lastWidth;
        float lastX = current.x, lastY = current.y;

        float xa, xb, ya, yb, x, y;
        for (float i = 0; i < 1; i += .02) {
            xa = previous.x + (previous.x - mid.x) * i;
            ya = previous.y + (previous.y - mid.y) * i;

            xb = mid.x + (current.x - mid.x) * i;
            yb = mid.y + (current.y - mid.y) * i;

            x = xa + ((xb - xa) * i);
            y = ya + ((yb - ya) * i);

            float width = points.lastWidth + diff * i;
            mPaint.setStrokeWidth(width);

            points.add(new DrawingPoint(x, y, (previous.time + current.time) / 2, width));
            mCanvas.drawLine(lastX, lastY, x, y, mPaint);

            lastX = x;
            lastY = y;
        }

        points.lastWidth = strokeWidth;
        points.lastVelocity = velocity;
    }

    public boolean redo() {
        if (!redoPoints.isEmpty()) {
            long start = SystemClock.currentThreadTimeMillis();

            DrawingPoints redone = redoPoints.pop();
            allPoints.push(redone);

            mBitmap.recycle();
            mBitmap = cachedBitmap.copy(cachedBitmap.getConfig(), true);
            mCanvas = new Canvas(mBitmap);

            for (DrawingPoints points: allPoints) {
                Paint redraw = points.redrawPaint;
                for (DrawingPoint point: points) {
                    redraw.setStrokeWidth(point.width);
                    mCanvas.drawPoint(point.x, point.y, redraw);
                }
            }

            Logg.log("ELAPSED: " + (SystemClock.currentThreadTimeMillis() - start) / 1000.0);
            return true;
        }
        return false;
    }

    public boolean undo() {
        if (!allPoints.isEmpty()) {
            long start = SystemClock.currentThreadTimeMillis();

            DrawingPoints undone = allPoints.pop();
            redoPoints.push(undone);

            mBitmap.recycle();
            mBitmap = cachedBitmap.copy(cachedBitmap.getConfig(), true);
            mCanvas = new Canvas(mBitmap);

            for (DrawingPoints points: allPoints) {
                for (DrawingPoint point: points) {
                    mCanvas.drawPoint(point.x, point.y, points.redrawPaint);
                }
            }

            Logg.log("ELAPSED: " + (SystemClock.currentThreadTimeMillis() - start) / 1000.0);
            return true;
        }
        return false;
    }

    public void erase() {
        if (mState == State.ERASE) {
            changeState(State.DRAW);
        } else {
            changeState(State.ERASE);
        }
    }

    public void onEvent(EventColorChosen eventColorChosen) {
        if (eventColorChosen.color != 0) {
            if (!eventColorChosen.which) {
                changeState(State.DRAW);

                reset(eventColorChosen.color);

                ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(),
                        currentBackgroundColor, eventColorChosen.color);
                colorAnimation.addUpdateListener(animator -> mCanvas.drawColor((Integer) animator.getAnimatedValue()));
                colorAnimation.setDuration(1000);
                colorAnimation.start();

                currentBackgroundColor = eventColorChosen.color;
            } else {
                changeState(State.DRAW);
                setPaintColor(eventColorChosen.color);
            }
        }
    }

    public void onEvent(EventBrushChosen eventBrushChosen) {
        changeState(State.DRAW);

        if (eventBrushChosen.paint != null) {
            if (eventBrushChosen.paint.getShader() != null) {
                eventBrushChosen.paint.setShader(null);
                changeState(State.RAINBOW);
            }
            int prevColor = mPaint.getColor();
            mPaint.set(eventBrushChosen.paint);
            mPaint.setColor(prevColor);
        }

        setPaintThickness(eventBrushChosen.thickness);
    }

    public ArrayList<Integer> getCurrentColors() { return colors; }

    public int getCurrentStrokeColor() { return currentStrokeColor; }

    public Bitmap getBitmap() { return mBitmap; }

    public float getBrushWidth() { return STROKE_WIDTH; }

    public void setPaintColor(int color) {
        currentStrokeColor = color;
        mPaint.setColor(currentStrokeColor);
    }

    public void setPaintThickness(float floater) {
        STROKE_WIDTH = floater;
        mPaint.setStrokeWidth(STROKE_WIDTH);
    }

    public void onSave() {
        FileUtils.cacheColors(mContext, colors);
        store.setLastBackgroundColor(currentBackgroundColor);
    }

    private static final Handler mHandler = new Handler();
    private Runnable paintRunnable = new Runnable() {
        public void run() {
            int prevColor = mPaint.getColor();
            int nextColor = rainbow[random.nextInt(rainbow.length)];
            ValueAnimator animator = ValueAnimator.ofObject(new ArgbEvaluator(),
                    prevColor, nextColor);
            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    mPaint.setColor((Integer) animation.getAnimatedValue());
                }
            });
            animator.setDuration(100);
            animator.start();

            mPaint.setColor(rainbow[random.nextInt(rainbow.length)]);
            mHandler.postDelayed(paintRunnable, 100);
        }
    };
}
