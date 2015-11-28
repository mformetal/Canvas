package milespeele.canvas.drawing;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Handler;
import android.os.SystemClock;
import android.text.TextPaint;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;

import java.util.Random;
import java.util.Set;
import java.util.Stack;

import javax.inject.Inject;

import de.greenrobot.event.EventBus;
import milespeele.canvas.MainApp;
import milespeele.canvas.event.EventBrushChosen;
import milespeele.canvas.event.EventColorChosen;
import milespeele.canvas.event.EventTextChosen;
import milespeele.canvas.paint.PaintStyles;
import milespeele.canvas.util.FileUtils;
import milespeele.canvas.util.Datastore;
import milespeele.canvas.util.Logg;
import milespeele.canvas.util.TextUtils;
import milespeele.canvas.util.ViewUtils;
import rx.schedulers.Schedulers;

/**
 * Created by mbpeele on 9/25/15.
 */
public class DrawingCurve {

    public enum State {
        DRAW,
        ERASE,
        RAINBOW,
        TEXT
    }

    private Rect textBounds;
    private ScaleGestureDetector scaleGestureDetector;
    private Bitmap mBitmap, cachedBitmap;
    private Canvas mCanvas;
    private DrawingPoints[] pointerPoints;
    private Stack<DrawingPoints> redoPoints, allPoints;
    private Paint mPaint;
    private TextPaint textPaint;
    private Random random;
    private State mState = State.DRAW;
    private final Context mContext;

    private static final float VELOCITY_FILTER_WEIGHT = 0.2f;
    private static float STROKE_WIDTH = 10f;
    private static final float POINT_MAX_WIDTH = 50f, POINT_MIN_WIDTH = 2f, POINT_TOLERANCE = 5f;
    private static final int MAX_POINTERS = 2;
    private float scaleFactor = 1;
    private int[] rainbow;
    private int activePointer = 0;
    private int currentStrokeColor, currentBackgroundColor;
    private int width, height;
    private String textToBeDrawn;
    private float textX, textY;
    private boolean isSafeToDraw = true;

    @Inject Datastore store;
    @Inject EventBus bus;

    private DrawingCurveListener listener;
    public interface DrawingCurveListener {
        void showButton(String text);
        void hideButton();
    }

    public DrawingCurve(Context context, int w, int h) {
        ((MainApp) context.getApplicationContext()).getApplicationComponent().inject(this);
        bus.register(this);

        width = w;
        height = h;

        mContext = context;

        random = new Random();

        rainbow = ViewUtils.rainbow();

        currentStrokeColor = Color.argb(255, random.nextInt(256), random.nextInt(256), random.nextInt(256));
        currentBackgroundColor = store.getLastBackgroundColor();

        cachedBitmap = FileUtils.getCachedBitmap(mContext);
        if (cachedBitmap == null) {
            cachedBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
            cachedBitmap.eraseColor(currentBackgroundColor);
        }

        mBitmap = cachedBitmap.copy(cachedBitmap.getConfig(), true);
        mCanvas = new Canvas(mBitmap);

        mPaint = PaintStyles.normal(currentStrokeColor, STROKE_WIDTH);

        textBounds = new Rect();
        textPaint = new TextPaint(TextPaint.ANTI_ALIAS_FLAG);
        textPaint.setColor(currentStrokeColor);
        textPaint.setStyle(Paint.Style.FILL_AND_STROKE);

        scaleGestureDetector = new ScaleGestureDetector(mContext, new ScaleListener());

        allPoints = new Stack<>();
        redoPoints = new Stack<>();
        pointerPoints = new DrawingPoints[MAX_POINTERS];
        for (int ndx = 0; ndx < pointerPoints.length; ndx++) {
            pointerPoints[ndx] = new DrawingPoints(mPaint);
        }
    }

    public void onButtonClicked() {
        switch (mState) {
            case TEXT:
                listener.hideButton();

                mCanvas.drawText(textToBeDrawn, textX, textY, textPaint);

                changeState(State.DRAW);
                break;
        }
    }

    public void setListener(DrawingCurveListener listener) {
        this.listener = listener;
    }

    public void drawToSurfaceView(Canvas canvas) {
        if (isSafeToDraw) {
            canvas.drawBitmap(mBitmap, 0, 0, null);

            switch (mState) {
                case TEXT:
                    canvas.drawText(textToBeDrawn, textX, textY, textPaint);
                    break;
            }
        }
    }

    public void changeState(State newValue) {
        mState = newValue;

        switch (mState) {
            case ERASE:
                mHandler.removeCallbacksAndMessages(null);
                mPaint.setColor(currentBackgroundColor);
                setPaintThickness(20f);
                break;
            case DRAW:
                mHandler.removeCallbacksAndMessages(null);
                mPaint.set(PaintStyles.normal(currentStrokeColor, mPaint.getStrokeWidth()));
                break;
            case RAINBOW:
                paintRunnable.run();
                break;
        }
    }

    public void reset(int color) {
        isSafeToDraw = false;

        for (DrawingPoints list: pointerPoints) {
            list.clear();
        }

        allPoints.clear();
        redoPoints.clear();

        cachedBitmap.recycle();
        cachedBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        cachedBitmap.eraseColor(color);

        mBitmap.recycle();
        mBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mBitmap);
        mCanvas.drawBitmap(cachedBitmap, 0, 0, null);

        ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(),
                currentBackgroundColor, color);
        colorAnimation.addUpdateListener(animator -> mCanvas.drawColor((Integer) animator.getAnimatedValue()));
        colorAnimation.setDuration(1000);
        colorAnimation.start();

        currentBackgroundColor = color;

        isSafeToDraw = true;
    }

    public void onTouchDown(MotionEvent event) {
        switch (mState) {
            case ERASE:
            case DRAW:
            case RAINBOW:
                downDraw(event);
                break;
            case TEXT:
                textX = event.getX() - textPaint.measureText(textToBeDrawn) / 2;
                textY = event.getY();
                break;
        }
    }

    public void downDraw(MotionEvent event) {
        activePointer = event.getPointerId(0);

        if (mState == State.ERASE) {
            addPoint(event.getX(), event.getY(), 0);
        } else {
            if (event.getPointerCount() > 1) {
                for (int p = 0; p < MAX_POINTERS; p++) {
                    addPoint(event.getX(p), event.getY(p), p);
                }
            } else {
                addPoint(event.getX(), event.getY(), activePointer);
            }
        }
    }

    public void onTouchMove(MotionEvent event) {
        switch (mState) {
            case ERASE:
            case DRAW:
            case RAINBOW:
                moveDraw(event);
                break;
            case TEXT:
                textX = event.getX() - textPaint.measureText(textToBeDrawn) / 2;
                textY = event.getY();
                break;
        }
    }

    public void moveDraw(MotionEvent event) {
        if (mState == State.ERASE) {
            for (int i = 0; i < event.getHistorySize(); i++) {
                addPoint(event.getHistoricalX(i), event.getHistoricalY(i), 0);
            }
            addPoint(event.getX(), event.getY(), 0);
            return;
        }

        if (event.getPointerCount() > 1) {
            for (int h = 0; h < event.getHistorySize(); h++) {
                for (int p = 0; p < MAX_POINTERS; p++) {
                    float x = event.getHistoricalX(p, h), y = event.getHistoricalY(p, h);
                    addPoint(x, y, p);
                }
            }
        } else {
            for (int i = 0; i < event.getHistorySize(); i++) {
                float x = event.getHistoricalX(i), y = event.getHistoricalY(i);
                addPoint(x, y, activePointer);
            }
            addPoint(event.getX(), event.getY(), activePointer);
        }
    }

    public void onTouchUp(MotionEvent event) {
        switch (mState) {
            case ERASE:
            case DRAW:
            case RAINBOW:
                upDraw(event);
                break;
            case TEXT:
                break;
        }
    }

    public void upDraw(MotionEvent event) {
        int pointer = (event.getAction() & MotionEvent.ACTION_POINTER_INDEX_MASK)
                >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
        int pointerId = event.getPointerId(pointer);
        if (mState == State.ERASE) {
            pointerId = 0;
        }

        if (pointerId < pointerPoints.length) {
            DrawingPoints pointsToClear = pointerPoints[pointerId];
            allPoints.push(new DrawingPoints(pointsToClear));
            pointsToClear.clear();
        }

        if (pointerId == activePointer && event.getPointerCount() > 1) {
            activePointer = 1;
        }
    }

    public void addPoint(float x, float y, int pointerId) {
        DrawingPoint prevPoint;
        DrawingPoint nextPoint = new DrawingPoint(x, y, SystemClock.currentThreadTimeMillis(),
                mPaint.getStrokeWidth(), mPaint.getColor());

        DrawingPoints points = pointerPoints[pointerId];
        if (points.isEmpty()) {
            float width = mPaint.getStrokeWidth();
            mPaint.setStrokeWidth(width / 2);
            mCanvas.drawPoint(x, y, mPaint);
            points.add(nextPoint);
            mPaint.setStrokeWidth(width);
        } else {
            prevPoint = points.getLast();

            if (Math.abs(prevPoint.x - x) < POINT_TOLERANCE && Math.abs(prevPoint.y - y) < POINT_TOLERANCE) {
                return;
            }

            points.add(nextPoint);
            algorithmDraw(prevPoint, nextPoint, points);
        }
    }

    private void algorithmDraw(DrawingPoint previous, DrawingPoint current, DrawingPoints points) {
        DrawingPoint mid = current.midPoint(previous);
        float velocity =  VELOCITY_FILTER_WEIGHT * current.velocityFrom(previous)
                + (1 - VELOCITY_FILTER_WEIGHT) * points.getLastVelocity();
        float strokeWidth = Math.abs(STROKE_WIDTH - velocity);
        if (strokeWidth < POINT_MIN_WIDTH) { strokeWidth = POINT_MIN_WIDTH; }
        if (strokeWidth > POINT_MAX_WIDTH) { strokeWidth = POINT_MAX_WIDTH; }
        float diff = strokeWidth - points.getLastWidth();

        float xa, xb, ya, yb, x, y;
        for (float i = 0; i < 1; i += (mPaint.getStrokeWidth() > 40f) ? .1 : .02) {
            xa = previous.x + (previous.x - mid.x) * i;
            ya = previous.y + (previous.y - mid.y) * i;

            xb = mid.x + (current.x - mid.x) * i;
            yb = mid.y + (current.y - mid.y) * i;

            x = xa + ((xb - xa) * i);
            y = ya + ((yb - ya) * i);

            float width = points.getLastWidth() + diff * i;
            if (mState != State.ERASE) {
                mPaint.setStrokeWidth(width);
            }

            points.add(new DrawingPoint(x, y, mid.time, mPaint.getStrokeWidth(), mPaint.getColor()));
            mCanvas.drawPoint(x, y, mPaint);
        }

        points.setLastWidth(strokeWidth);
        points.setLastVelocity(velocity);
    }

    public boolean redo() {
        if (!redoPoints.isEmpty()) {
            long start = SystemClock.currentThreadTimeMillis();

            DrawingPoints redone = redoPoints.pop();
            allPoints.push(new DrawingPoints(redone));

            mCanvas.drawBitmap(cachedBitmap, 0, 0, null);

            for (DrawingPoints points: allPoints) {
                Paint redraw = points.getRedrawPaint();
                for (DrawingPoint point: points) {
                    redraw.setStrokeWidth(point.width);
                    redraw.setColor(point.color);
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
            redoPoints.push(new DrawingPoints(undone));

            mCanvas.drawBitmap(cachedBitmap, 0, 0, null);

            for (DrawingPoints points: allPoints) {
                Paint redraw = points.getRedrawPaint();
                for (DrawingPoint point: points) {
                    redraw.setStrokeWidth(point.width);
                    redraw.setColor(point.color);
                    mCanvas.drawPoint(point.x, point.y, redraw);
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

    public void onEvent(EventTextChosen eventTextChosen) {
        textToBeDrawn = eventTextChosen.text;
        textPaint.setColor(currentStrokeColor);

        TextUtils.adjustTextSize(textPaint, textToBeDrawn, height);
        TextUtils.adjustTextScale(textPaint, textToBeDrawn, width, 0, 0);

        textX = (width - textPaint.measureText(textToBeDrawn)) / 2;
        textY = height / 2;

        changeState(State.TEXT);

        listener.showButton("DROP");
    }

    public void onEvent(EventColorChosen eventColorChosen) {
        if (eventColorChosen.color != 0) {
            if (!eventColorChosen.which) {
                changeState(State.DRAW);

                reset(eventColorChosen.color);
            } else {
                changeState(State.DRAW);

                setPaintColor(eventColorChosen.color);
            }
        }
    }

    public void onEvent(EventBrushChosen eventBrushChosen) {
        Paint newPaint = eventBrushChosen.paint;

        if (newPaint.getShader() != null) {
            newPaint.setShader(null);
            changeState(State.RAINBOW);
        } else {
            changeState(State.DRAW);
        }

        int prevColor = mPaint.getColor();
        mPaint.set(newPaint);
        mPaint.setColor(prevColor);

        for (DrawingPoints points : pointerPoints) {
            points.setRedrawPaint(mPaint);
        }

        setPaintThickness(newPaint.getStrokeWidth());
    }

    public int getCurrentStrokeColor() { return currentStrokeColor; }

    public Bitmap getBitmap() { return mBitmap; }

    public Paint getCurrentPaint() { return mPaint; }

    public void setPaintColor(int color) {
        currentStrokeColor = color;
        mPaint.setColor(currentStrokeColor);

        for (DrawingPoints points: pointerPoints) {
            points.getRedrawPaint().setColor(currentStrokeColor);
        }
    }

    public void setPaintThickness(float floater) {
        STROKE_WIDTH = floater;
        mPaint.setStrokeWidth(STROKE_WIDTH);

        for (DrawingPoints points: pointerPoints) {
            points.getRedrawPaint().setStrokeWidth(STROKE_WIDTH);
        }
    }

    public void onSave() {
        store.setLastBackgroundColor(currentBackgroundColor);

        FileUtils.compressBitmapAsObservable(mBitmap)
                .subscribeOn(Schedulers.newThread())
                .observeOn(Schedulers.immediate())
                .subscribe(bytes -> {
                    Logg.log("CALL");
                    FileUtils.cacheBitmap(mContext, bytes);
                });
    }

    private static final Handler mHandler = new Handler();
    private Runnable paintRunnable = new Runnable() {
        public void run() {
            mPaint.setColor(rainbow[random.nextInt(rainbow.length)]);
            mHandler.postDelayed(this, 100);
        }
    };

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {

        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            scaleFactor *= detector.getScaleFactor();

            scaleFactor = Math.max(.01f, Math.min(scaleFactor, 5.0f));

            return true;
        }
    }
}
