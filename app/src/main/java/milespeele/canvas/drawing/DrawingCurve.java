package milespeele.canvas.drawing;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Handler;
import android.os.SystemClock;
import android.view.MotionEvent;

import java.util.ArrayList;
import java.util.Random;
import java.util.Stack;

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
import rx.android.schedulers.AndroidSchedulers;

/**
 * Created by mbpeele on 9/25/15.
 */
public class DrawingCurve implements PaintStore.PaintStoreListener {

    public enum State {
        DRAW,
        ERASE,
        RAINBOW
    }

    private final RectF dirtyRect = new RectF();
    private final Rect dirty = new Rect();
    private Bitmap mBitmap, cachedBitmap;
    private Canvas mCanvas;
    private DrawingPoints[] pointerPoints;
    private Stack<DrawingPoints> allPoints, redoPoints;
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
    private float lastTouchX, lastTouchY;
    private float eraseX, eraseY;
    private boolean isSafeToDraw = true;

    @Inject Datastore store;
    @Inject EventBus bus;

    private DrawingCurveListener listener;
    public interface DrawingCurveListener {
    }

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

        allPoints = new Stack<>();
        redoPoints = new Stack<>();
        pointerPoints = new DrawingPoints[5];
        for (int ndx = 0; ndx < 5; ndx++) {
            pointerPoints[ndx] = new DrawingPoints(mPaint);
        }
    }

    @Override
    public void onColorChanged(int newColor) {
        colors.add(newColor);
    }

    public void setListener(DrawingCurveListener listener) {
        this.listener = listener;
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
                mPaint.changePaint(PaintStyles.normal(currentStrokeColor, mPaint.getStrokeWidth()));
                break;
            case RAINBOW:
                paintRunnable.run();
                break;
        }
    }

    public void reset(int color) {
        isSafeToDraw = false;

        for (ArrayList list: pointerPoints) {
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

    public Rect getDirtyRect() {
        dirty.set(Math.round(dirtyRect.left - mPaint.getStrokeWidth() / 2),
                Math.round(dirtyRect.top - mPaint.getStrokeWidth() / 2),
                Math.round(dirtyRect.right + mPaint.getStrokeWidth() / 2),
                Math.round(dirtyRect.bottom + mPaint.getStrokeWidth() / 2));
        return dirty;
    }

    public void expandDirtyRect(float historicalX, float historicalY) {
        if (historicalX < dirtyRect.left) {
            dirtyRect.left = historicalX;
        } else if (historicalX > dirtyRect.right) {
            dirtyRect.right = historicalX;
        }
        if (historicalY < dirtyRect.top) {
            dirtyRect.top = historicalY;
        } else if (historicalY > dirtyRect.bottom) {
            dirtyRect.bottom = historicalY;
        }
    }

    public void resetDirtyRect(float eventX, float eventY) {
        dirtyRect.left = Math.min(lastTouchX, eventX);
        dirtyRect.right = Math.max(lastTouchX, eventX);
        dirtyRect.top = Math.min(lastTouchY, eventY);
        dirtyRect.bottom = Math.max(lastTouchY, eventY);
    }

    public void resetDirtyRect() {
        dirtyRect.set(0, 0, 0, 0);
    }

    public void onTouchDown(MotionEvent event) {
        if (mState == State.ERASE) {
            eraseX = event.getX();
            eraseY = event.getY();
            mCanvas.drawPoint(eraseX, eraseY, mPaint);
        } else {
            if (event.getPointerCount() > 1) {
                for (int p = 0; p < event.getPointerCount(); p++) {
                    addPoint(event.getX(p), event.getY(p), event.getPointerId(p));
                }
            } else {
                addPoint(event.getX(), event.getY(), 0);
            }
        }
    }

    public void onTouchMove(MotionEvent event) {
        if (mState == State.ERASE) {
            float x = event.getX(), y = event.getY();
            if ( Math.abs(x - eraseX) >= POINT_TOLERANCE || Math.abs(y - eraseY) >= POINT_TOLERANCE) {
                mCanvas.drawLine(eraseX, eraseY, x, y, mPaint);
                eraseX = x;
                eraseY = y;
            }
        } else {
            resetDirtyRect(event.getX(), event.getY());

            int count = event.getPointerCount();

            if (count > 1) {
                if (count > 3) {
                    count -= 1;
                }

                for (int h = 0; h < event.getHistorySize(); h++) {
                    for (int p = 0; p < count; p++) {
                        float historicalX = event.getHistoricalX(p, h);
                        float historicalY = event.getHistoricalY(p, h);

                        expandDirtyRect(historicalX, historicalY);
                        addPoint(event.getHistoricalX(p, h), event.getHistoricalY(p, h), event.getPointerId(p));
                    }
                }
            } else {
                for (int i = 0; i < event.getHistorySize(); i++) {
                    addPoint(event.getHistoricalX(i), event.getHistoricalY(i), 0);
                }
                addPoint(event.getX(), event.getY(), 0);
            }
        }
    }

    public void onTouchUp(MotionEvent event) {
        if (mState == State.ERASE) {
        } else {
            final int pointer = (event.getAction() & MotionEvent.ACTION_POINTER_INDEX_MASK)
                    >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;

            DrawingPoints pointsToClear = pointerPoints[pointer];
            allPoints.push(new DrawingPoints(pointsToClear));
            pointsToClear.clear();

            lastTouchX = event.getX();
            lastTouchY = event.getY();
        }
    }

    public void addPoint(float x, float y, int pointerId) {
        if (mState == State.ERASE) {
            mCanvas.drawPoint(x, y, mPaint);
            return;
        }

        DrawingPoint prevPoint;
        DrawingPoint nextPoint = new DrawingPoint(x, y, SystemClock.currentThreadTimeMillis(),
                mPaint.getStrokeWidth(), mPaint.getColor());

        DrawingPoints points = pointerPoints[pointerId];
        if (points.isEmpty()) {
            mCanvas.drawPoint(x, y, mPaint);
            points.add(nextPoint);
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
        float lastX = current.x, lastY = current.y;

        float xa, xb, ya, yb, x, y;
        for (float i = 0; i < 1; i += .02) {
            xa = previous.x + (previous.x - mid.x) * i;
            ya = previous.y + (previous.y - mid.y) * i;

            xb = mid.x + (current.x - mid.x) * i;
            yb = mid.y + (current.y - mid.y) * i;

            x = xa + ((xb - xa) * i);
            y = ya + ((yb - ya) * i);

            float width = points.getLastWidth() + diff * i;
            mPaint.setStrokeWidth(width);

            points.add(new DrawingPoint(x, y, (previous.time + current.time) / 2, width, mPaint.getColor()));
            mCanvas.drawLine(lastX, lastY, x, y, mPaint);

            lastX = x;
            lastY = y;
        }

        points.setLastWidth(strokeWidth);
        points.setLastVelocity(velocity);
    }

    public boolean redo() {
        if (!redoPoints.isEmpty()) {
            resetDirtyRect();
            long start = SystemClock.currentThreadTimeMillis();

            DrawingPoints redone = redoPoints.pop();
            allPoints.push(redone);

            mCanvas.drawBitmap(cachedBitmap, 0, 0, null);

            for (DrawingPoints points: allPoints) {
                Paint redraw = points.getRedrawPaint();
                for (DrawingPoint point: points) {
                    redraw.setStrokeWidth(point.width);
                    redraw.setColor(point.color);
                    mCanvas.drawPoint(point.x, point.y, redraw);
                    expandDirtyRect(point.x, point.y);
                }
            }

            Logg.log("ELAPSED: " + (SystemClock.currentThreadTimeMillis() - start) / 1000.0);
            return true;
        }
        return false;
    }

    public boolean undo() {
        if (!allPoints.isEmpty()) {
            resetDirtyRect();
            long start = SystemClock.currentThreadTimeMillis();

            DrawingPoints undone = allPoints.pop();
            redoPoints.push(undone);

            mCanvas.drawBitmap(cachedBitmap, 0, 0, null);

            for (DrawingPoints points: allPoints) {
                Paint redraw = points.getRedrawPaint();
                for (DrawingPoint point: points) {
                    redraw.setStrokeWidth(point.width);
                    redraw.setColor(point.color);
                    mCanvas.drawPoint(point.x, point.y, redraw);
                    expandDirtyRect(point.x, point.y);
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

            for (DrawingPoints points: pointerPoints) {
                points.setRedrawPaint(mPaint);
            }
        }

        setPaintThickness(eventBrushChosen.thickness);
    }

    public boolean iSafeToDraw() {
        return isSafeToDraw;
    }

    public ArrayList<Integer> getCurrentColors() { return colors; }

    public int getCurrentStrokeColor() { return currentStrokeColor; }

    public Bitmap getBitmap() { return mBitmap; }

    public float getBrushWidth() { return mPaint.getStrokeWidth(); }

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
        FileUtils.cacheColors(mContext, colors);
        store.setLastBackgroundColor(currentBackgroundColor);

        FileUtils.compressBitmapAsObservable(mBitmap)
                .subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(bytes -> {
                    FileUtils.cacheBitmap(mContext, bytes);
                });
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

            mHandler.postDelayed(paintRunnable, 100);
        }
    };
}
