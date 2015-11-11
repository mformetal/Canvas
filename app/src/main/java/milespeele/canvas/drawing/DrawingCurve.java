package milespeele.canvas.drawing;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.os.Environment;
import android.os.Handler;
import android.os.SystemClock;
import android.view.MotionEvent;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

import javax.inject.Inject;

import de.greenrobot.event.EventBus;
import milespeele.canvas.MainApp;
import milespeele.canvas.R;
import milespeele.canvas.event.EventBrushChosen;
import milespeele.canvas.event.EventColorChosen;
import milespeele.canvas.paint.PaintStyles;
import milespeele.canvas.paint.PaintStore;
import milespeele.canvas.util.FileUtils;
import milespeele.canvas.util.Datastore;
import milespeele.canvas.util.Logg;

/**
 * Created by mbpeele on 9/25/15.
 */
public class DrawingCurve implements PaintStore.PaintStoreListener {

    public enum State {
        DRAW,
        ERASE,
        RAINBOW
    }

    private final RectF inkRect = new RectF();
    private Bitmap mBitmap;
    private Canvas mCanvas;
    private DrawingPoints currentPoints;
    private DrawingHistory redoPoints, allPoints;
    private PaintStore mPaint;
    private Random random;
    private State mState = State.DRAW;
    private Context mContext;

    private static final float VELOCITY_FILTER_WEIGHT = 0.2f;
    private static float STROKE_WIDTH = 10f;
    private static final float POINT_MAX_WIDTH = 50f, POINT_MIN_WIDTH = 2f, POINT_TOLERANCE = 5f;
    private float lastWidth, lastVelocity;
    private int[] rainbow;
    private int currentStrokeColor, currentBackgroundColor;
    private boolean canDraw = true;

    @Inject Datastore store;
    @Inject EventBus bus;

    public DrawingCurve(Context context, int w, int h) {
        ((MainApp) context.getApplicationContext()).getApplicationComponent().inject(this);
        bus.register(this);

        mContext = context;

        random = new Random();

        rainbow = getRainbowColors();

        currentStrokeColor = Color.argb(255, random.nextInt(256), random.nextInt(256), random.nextInt(256));
        currentBackgroundColor = store.getLastBackgroundColor();

        createInkRect(w, h);
        Bitmap cachedBitmap = FileUtils.getCachedBitmap(mContext);
        mBitmap = cachedBitmap != null ? cachedBitmap :
                Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        if (cachedBitmap == null) {
            mBitmap.eraseColor(currentBackgroundColor);
        }
        mCanvas = new Canvas(mBitmap);
        mCanvas.drawBitmap(mBitmap, 0, 0, null);

        mPaint = new PaintStore(currentStrokeColor, STROKE_WIDTH);
        mPaint.setListener(this);


        currentPoints = new DrawingPoints(mPaint);
        allPoints = new DrawingHistory();
        redoPoints = new DrawingHistory();
    }

    @Override
    public void onColorChanged(int newColor) {
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

    public void hardReset(int color) {
        canDraw = false;

        currentPoints.clear();
        allPoints.clear();
        redoPoints.clear();

        lastWidth = 0;
        lastVelocity = 0;

        mBitmap.recycle();
        mBitmap = Bitmap.createBitmap(mBitmap.getWidth(), mBitmap.getHeight(), Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mBitmap);

        mCanvas.drawColor(color, PorterDuff.Mode.CLEAR);

        canDraw = true;
    }

    public void drawToViewCanvas(Canvas canvas) {
        if (canDraw) {
            canvas.drawBitmap(mBitmap, 0, 0, null);
        }
    }

    public void onTouchUp(float eventX, float eventY) {
        lastVelocity = 0;
        lastWidth = 0;

        addPoint(eventX, eventY);

        allPoints.push(currentPoints);
        currentPoints.clear();
    }

    public void parseMotionEvent(MotionEvent event) {
        for (int i = 0; i < event.getHistorySize(); i++) {
            addPoint(event.getHistoricalX(i), event.getHistoricalY(i));
        }
        addPoint(event.getX(), event.getY());
    }

    public void addPoint(float x, float y) {
        DrawingPoint prevPoint = null;
        if (!currentPoints.isEmpty()) {
            prevPoint = currentPoints.peek();
            if (Math.abs(prevPoint.x - x) < POINT_TOLERANCE && Math.abs(prevPoint.y - y) < POINT_TOLERANCE) {
                return;
            }
        }

        DrawingPoint toAdd = new DrawingPoint(x, y, SystemClock.currentThreadTimeMillis(),
                mPaint.getStrokeWidth());
        toAdd.width = mPaint.getStrokeWidth();
        currentPoints.add(toAdd);

        if (prevPoint == null) {
            mCanvas.drawPoint(x, y, mPaint);
        } else {
            switch (mState) {
                case DRAW:
                case RAINBOW:
                    algorithmDraw(prevPoint, toAdd);
                    break;
                case ERASE:
                    mCanvas.drawLine(prevPoint.x, prevPoint.y, toAdd.x, toAdd.y, mPaint);
                    break;
            }
        }
    }

    private void algorithmDraw(DrawingPoint previous, DrawingPoint current) {
        DrawingPoint mid = current.midPoint(previous);

        float velocity =  VELOCITY_FILTER_WEIGHT * current.velocityFrom(previous)
                + (1 - VELOCITY_FILTER_WEIGHT) * lastVelocity;
        float strokeWidth = Math.abs(STROKE_WIDTH - velocity);
        if (strokeWidth < POINT_MIN_WIDTH) { strokeWidth = POINT_MIN_WIDTH; }
        if (strokeWidth > POINT_MAX_WIDTH) { strokeWidth = POINT_MAX_WIDTH; }
        float diff = strokeWidth - lastWidth;
        float lastX = current.x, lastY = current.y;

        float xa, xb, ya, yb, x, y;
        for (float i = 0; i < 1; i += .02) {
            xa = previous.x + (previous.x - mid.x) * i;
            ya = previous.y + (previous.y - mid.y) * i;

            xb = mid.x + (current.x - mid.x) * i;
            yb = mid.y + (current.y - mid.y) * i;

            x = xa + ((xb - xa) * i);
            y = ya + ((yb - ya) * i);

            mPaint.setStrokeWidth(lastWidth + diff * i);

            currentPoints.add(new DrawingPoint(x, y, (previous.time + current.time) / 2,
                    mPaint.getStrokeWidth()));
            mCanvas.drawLine(lastX, lastY, x, y, mPaint);

            lastX = x;
            lastY = y;
        }

        lastWidth = strokeWidth;
        lastVelocity = velocity;
    }

    public void redo() {
        if (!redoPoints.isEmpty()) {
            DrawingPoints points = redoPoints.pop();
            allPoints.push(points);

            redraw(points, true);
        }
    }

    public void undo() {
        if (!allPoints.isEmpty()) {
            DrawingPoints points = allPoints.pop();
            redoPoints.push(points);

            redraw(points, false);
        }
    }

    private void redraw(DrawingPoints points, boolean toRedo) {
        long start = SystemClock.elapsedRealtimeNanos();

        if (toRedo) {
            for (DrawingPoint point: points) {
                mPaint.setStrokeWidth(point.width);
                mCanvas.drawPoint(point.x, point.y, mPaint);
            }
        } else {
            Paint paint = new Paint(mPaint);
            paint.setColor(currentBackgroundColor);

            for (int i = 0; i < points.size() - 1; i++) {
                DrawingPoint cur = points.get(i);
                DrawingPoint next = points.get(i + 1);
                paint.setStrokeWidth(Math.max(cur.width, next.width));
                mCanvas.drawLine(cur.x, cur.y, next.x, next.y, paint);
            }
        }

        Logg.log("ELAPSED: " + (SystemClock.elapsedRealtimeNanos() - start) / 1000000000.0);
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
            if (eventColorChosen.which) {
                changeState(State.DRAW);

                hardReset(eventColorChosen.color);

                ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(),
                        currentBackgroundColor, eventColorChosen.color);
                colorAnimation.addUpdateListener(animator -> mCanvas.drawColor((Integer) animator.getAnimatedValue()));
                colorAnimation.setDuration(1000);
                colorAnimation.start();

                currentBackgroundColor = eventColorChosen.color;
            } else {
                changeState(State.DRAW);
                setPaintAlpha(eventColorChosen.opacity);
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

    public boolean canDraw() {
        return canDraw;
    }

    public int[] getRainbowColors() {
        return new int[] {
                Color.RED,
                Color.parseColor("#FF7F00"),
                Color.YELLOW,
                Color.GREEN,
                Color.BLUE,
                Color.parseColor("#4B0082"),
                Color.parseColor("#8B00FF")
        };
    }

    public int getCurrentStrokeColor() { return currentStrokeColor; }

    public Bitmap getBitmap() { return mBitmap; }

    public float getBrushWidth() { return STROKE_WIDTH; }

    public void setPaintAlpha(int opacity) {
        mPaint.setAlpha(opacity);
    }

    public void setPaintColor(int color) {
        currentStrokeColor = color;
        mPaint.setColor(currentStrokeColor);
    }

    public void setPaintThickness(float floater) {
        STROKE_WIDTH = floater;
        mPaint.setStrokeWidth(STROKE_WIDTH);
    }

    private void createInkRect(int w, int h) {
        if (w < h) {
            inkRect.left = w / 40;
            inkRect.top = w / 40;
            inkRect.right = w / 5;
            inkRect.bottom = w / 5;
        } else {
            inkRect.left = h / 40;
            inkRect.top = h / 40;
            inkRect.right = h / 5;
            inkRect.bottom = h / 5;
        }
    }

    public boolean inRange(float x, float y) {
        boolean xRange = x > 0 && x < mBitmap.getWidth();
        boolean yRange = y > 0 && y < mBitmap.getHeight();
        return xRange && yRange;
    }

    public void saveBackgroundColor() {
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
