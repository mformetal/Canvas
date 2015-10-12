package milespeele.canvas.drawing;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.AvoidXfermode;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelXorXfermode;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.Xfermode;
import android.os.Handler;
import android.os.SystemClock;
import android.view.MotionEvent;

import java.util.Random;

import javax.inject.Inject;

import de.greenrobot.event.EventBus;
import milespeele.canvas.MainApp;
import milespeele.canvas.R;
import milespeele.canvas.event.EventColorChosen;
import milespeele.canvas.event.EventRedo;
import milespeele.canvas.event.EventShowColorize;
import milespeele.canvas.event.EventShowErase;
import milespeele.canvas.event.EventUndo;
import milespeele.canvas.paint.PaintStyles;
import milespeele.canvas.util.BitmapUtils;
import milespeele.canvas.util.Datastore;
import milespeele.canvas.util.Logg;

/**
 * Created by mbpeele on 9/25/15.
 */
public class DrawingCurve {

    public enum State {
        DRAW,
        INK,
        ERASE,
        RAINBOW
    }

    private final RectF inkRect = new RectF();
    private Bitmap mBitmap;
    private Canvas mCanvas;
    private DrawingPoints currentPoints;
    private DrawingHistory redoPoints, allPoints;
    private Paint mPaint, inkPaint;
    private Random random;
    private State mState = State.DRAW;
    private Context mContext;

    private static final float VELOCITY_FILTER_WEIGHT = 0.2f;
    private static float STROKE_WIDTH = 10f;
    private static final float POINT_MAX_WIDTH = 50f, POINT_MIN_WIDTH = 2f;
    private static final float POINT_TOLERANCE = 5f;
    private int width, height;
    private float lastTouchX, lastTouchY, lastWidth, lastVelocity;
    private int[] rainbow;
    private int currentStrokeColor, currentBackgroundColor;
    private boolean canDraw = true;

    @Inject Datastore store;
    @Inject EventBus bus;

    public DrawingCurve(Context context, int w, int h) {
        ((MainApp) context.getApplicationContext()).getApplicationComponent().inject(this);
        bus.register(this);

        mContext = context;

        width = w;
        height = h;

        random = new Random();
        rainbow = getRainbowColors();

        currentStrokeColor = Color.argb(255, random.nextInt(256), random.nextInt(256), random.nextInt(256));
        currentBackgroundColor = store.getLastBackgroundColor();

        createInkRect(w, h);
        Bitmap cachedBitmap = BitmapUtils.getCachedBitmap(mContext);
        mBitmap = cachedBitmap != null ? Bitmap.createBitmap(cachedBitmap) :
                Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mBitmap);
        mCanvas.drawBitmap(mBitmap, 0, 0, null);

        mPaint = PaintStyles.normal(currentStrokeColor, STROKE_WIDTH);
        inkPaint = PaintStyles.normal(currentStrokeColor, STROKE_WIDTH);
        inkPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        inkPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP));

        currentPoints = new DrawingPoints(mPaint);
        allPoints = new DrawingHistory();
        redoPoints = new DrawingHistory();
    }

    public void changeState(State newValue) {
        mState = newValue;

        switch (mState) {
            case ERASE:
                mHandler.removeCallbacksAndMessages(null);
                mPaint = PaintStyles.erase(currentBackgroundColor, 20f);
                break;
            case DRAW:
                mPaint = PaintStyles.normal(currentStrokeColor, STROKE_WIDTH);
                break;
            case INK:
                mHandler.removeCallbacksAndMessages(null);
                inkPaint.setColor(currentStrokeColor);
                break;
            case RAINBOW:
                paintRunnable.run();
                break;
        }
    }

    public void resize(int width, int height) {

    }

    public void hardReset(int color) {
        canDraw = false;

        currentPoints.clear();
        allPoints.clear();
        redoPoints.clear();

        lastTouchX = 0;
        lastTouchY = 0;
        lastWidth = 0;
        lastVelocity = 0;

        mBitmap.recycle();
        mBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mBitmap);

        mCanvas.drawColor(color, PorterDuff.Mode.CLEAR);

        canDraw = true;
    }

    public void drawToViewCanvas(Canvas canvas) {
        canvas.drawBitmap(mBitmap, 0, 0, null);

        if (mState == State.INK) {
            canvas.drawRect(inkRect, inkPaint);
        }
    }

    public void onTouchUp(float eventX, float eventY) {
        lastTouchX = eventX;
        lastTouchY = eventY;
        lastVelocity = 0;
        lastWidth = 0;

        addPoint(eventX, eventY);

        onInkPaintTouchUp();

        allPoints.push(currentPoints);
        currentPoints.clear();
    }

    public void parseMotionEvent(MotionEvent event) {
        if (mState != State.INK) {
            for (int i = 0; i < event.getHistorySize(); i++) {
                addPoint(event.getHistoricalX(i), event.getHistoricalY(i));
            }
            addPoint(event.getX(), event.getY());
        } else {
            setInkPaintColorBasedOnPixel(event.getX(), event.getY());
        }
    }

    public void addPoint(float x, float y) {
        if (mState != State.INK) {
            DrawingPoint prevPoint = null;
            if (!currentPoints.isEmpty()) {
                currentPoints.paint = new Paint(mPaint);
                prevPoint = currentPoints.peek();
                if (Math.abs(prevPoint.x - x) < POINT_TOLERANCE && Math.abs(prevPoint.y - y) < POINT_TOLERANCE) {
                    return;
                }
            }

            DrawingPoint toAdd = new DrawingPoint(x, y, SystemClock.currentThreadTimeMillis(),
                    mPaint.getStrokeWidth());
            toAdd.width = mPaint.getStrokeWidth();
            toAdd.undo = averagePixel(x, y);
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
    }

    private void algorithmDraw(DrawingPoint previous, DrawingPoint current) {
        DrawingPoint mid = current.midPoint(previous);

        float velocity =  VELOCITY_FILTER_WEIGHT * current.velocityFrom(previous)
                + (1 - VELOCITY_FILTER_WEIGHT) * lastVelocity;
        float strokeWidth = Math.abs(STROKE_WIDTH - velocity);
        if (strokeWidth < POINT_MIN_WIDTH) { strokeWidth = POINT_MIN_WIDTH; }
        if (strokeWidth > POINT_MAX_WIDTH) { strokeWidth = POINT_MAX_WIDTH; }
        float diff = strokeWidth - lastWidth;

        int undo = averagePixel(current.x, current.y);
        float xa, xb, ya, yb, x, y;
        for (float i = 0; i < 1; i += .01) {
            xa = previous.x + (previous.x - mid.x) * i;
            ya = previous.y + (previous.y - mid.y) * i;

            xb = mid.x + (current.x - mid.x) * i;
            yb = mid.y + (current.y - mid.y) * i;

            x = xa + ((xb - xa) * i);
            y = ya + ((yb - ya) * i);

            mPaint.setStrokeWidth(lastWidth + diff * i);

            DrawingPoint point = new DrawingPoint(x, y, (previous.time + current.time) / 2,
                    mPaint.getStrokeWidth());
            point.undo = undo;
            currentPoints.add(point);
            mCanvas.drawPoint(x, y, mPaint);
        }

        lastWidth = strokeWidth;
        lastVelocity = velocity;
    }

    private int averagePixel(float x, float y) {
        int roundedX = Math.round(x);
        int roundedY = Math.round(y);

        int averagePixel = 0;
        int iterations = 0;
        for (int yIter = roundedY - 1; yIter < roundedY + 2; yIter++) {
            for (int xIter = roundedX - 1; xIter < roundedX + 2; xIter++) {
                if (xIter < mBitmap.getWidth() && yIter < mBitmap.getHeight()) {
                    averagePixel += mBitmap.getPixel(xIter, yIter);
                    iterations += 1;
                }
            }
        }

        return averagePixel / iterations;
    }

    public void onEvent(EventRedo eventRedo) {
        if (!redoPoints.isEmpty()) {
            DrawingPoints points = redoPoints.pop();
            allPoints.push(points);

            redraw(points, true);
        }
    }

    public void onEvent(EventUndo undo) {
        if (!allPoints.isEmpty()) {
            DrawingPoints points = allPoints.pop();
            redoPoints.push(points);

            redraw(points, false);
        }
    }

    private void redraw(DrawingPoints points, boolean toRedo) {
        long start = SystemClock.elapsedRealtimeNanos();

        if (toRedo) {
            points.paint.setStyle(Paint.Style.FILL_AND_STROKE);

            for (DrawingPoint point: points) {
                points.paint.setStrokeWidth(point.width);
                mCanvas.drawPoint(point.x, point.y, points.paint);
            }
        } else {
            Paint paint = PaintStyles.erase(Color.TRANSPARENT, mPaint.getStrokeWidth());
            paint.setStyle(Paint.Style.FILL);

            for (DrawingPoint point: points) {
                paint.setStrokeWidth(point.width + 1f);
                paint.setColor(point.undo);
                mCanvas.drawPoint(point.x, point.y, paint);
            }
        }

        Logg.log("ELAPSED: " + (SystemClock.elapsedRealtimeNanos() - start) / 1000000000.0);
    }

    public void onEvent(EventShowErase eventShowErase) {
        if (mState == State.ERASE) {
            changeState(State.DRAW);
        } else {
            changeState(State.ERASE);
        }
    }

    public void onEvent(EventShowColorize eventColorize) {
        if (mState == State.INK) {
            changeState(State.DRAW);
        } else {
            changeState(State.INK);
        }
    }

    public void onEvent(EventColorChosen eventColorChosen) {
        if (eventColorChosen.color != 0) {
            if (eventColorChosen.which.equals(mContext.getResources().getString(R.string.TAG_FRAGMENT_FILL))) {
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

    public boolean isCanDraw() {
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

    public void setInkPaintColorBasedOnPixel(float eventX, float eventY) {
        int color = mBitmap.getPixel(Math.round(eventX), Math.round(eventY));
        int colorToChangeTo;
        if (color != currentBackgroundColor)  {
            colorToChangeTo = (color == 0) ? currentStrokeColor : color;
        } else {
            colorToChangeTo = currentStrokeColor;
        }

        inkPaint.setColor(colorToChangeTo);
    }

    public void onInkPaintTouchUp() {
        if (mState == State.INK) {
            int color = inkPaint.getColor();

            if (color != currentBackgroundColor) {
                setPaintColor((color == 0) ? currentStrokeColor : color);
            } else {
                setPaintColor(currentStrokeColor);
            }

            changeState(State.DRAW);
        }
    }

    public State getState() { return mState; }

    public Bitmap getBitmap() { return mBitmap; }

    public void setPaintAlpha(int opacity) {
        mPaint.setAlpha(opacity);
    }

    public void setPaintColor(int color) {
        currentStrokeColor = color;
        mPaint.setColor(currentStrokeColor);
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

    public void onSave() {
        store.setLastBackgroundColor(currentBackgroundColor);
    }

    private static final Handler mHandler = new Handler();
    private Runnable paintRunnable = new Runnable() {
        public void run() {
            mPaint.setColor(rainbow[random.nextInt(rainbow.length)]);
            mHandler.postDelayed(paintRunnable, 100);
        }
    };
}
