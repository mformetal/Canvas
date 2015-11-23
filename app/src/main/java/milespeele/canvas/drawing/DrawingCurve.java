package milespeele.canvas.drawing;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Handler;
import android.os.SystemClock;
import android.view.MotionEvent;

import com.squareup.picasso.Cache;

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

    private Bitmap mBitmap;
    private Canvas mCanvas;
    private ArrayList<DrawingPoint>[] pointerPoints;
    private PaintStore mPaint;
    private Random random;
    private State mState = State.DRAW;
    private ArrayList<Integer> colors;
    private Context mContext;

    private static final float VELOCITY_FILTER_WEIGHT = 0.2f;
    private static float STROKE_WIDTH = 10f;
    private static final float POINT_MAX_WIDTH = 50f, POINT_MIN_WIDTH = 2f, POINT_TOLERANCE = 5f;
    private float lastWidth, lastVelocity;
    private int[] rainbow;
    private int currentStrokeColor, currentBackgroundColor;
    private boolean canDraw = true;
    private int historyCounter = 0;

    @Inject Datastore store;
    @Inject EventBus bus;
    @Inject Cache cache;

    public DrawingCurve(Context context, int w, int h) {
        ((MainApp) context.getApplicationContext()).getApplicationComponent().inject(this);
        bus.register(this);

        mContext = context;

        random = new Random();

        rainbow = ViewUtils.rainbow();

        colors = FileUtils.getColors(mContext);

        currentStrokeColor = Color.argb(255, random.nextInt(256), random.nextInt(256), random.nextInt(256));
        currentBackgroundColor = store.getLastBackgroundColor();

        Bitmap cachedBitmap = FileUtils.getCachedBitmap(mContext);
        mBitmap = cachedBitmap != null ? cachedBitmap :
                Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mBitmap);
        if (cachedBitmap == null) {
            mBitmap.eraseColor(currentBackgroundColor);
        }
        cache.set(String.valueOf(historyCounter), mBitmap);

        mPaint = new PaintStore(currentStrokeColor, STROKE_WIDTH);
        mPaint.setListener(this);

        pointerPoints = new ArrayList[4];
        for (int ndx = 0; ndx < 4; ndx++) {
            pointerPoints[ndx] = new ArrayList<>();
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
        canDraw = false;

        for (ArrayList list: pointerPoints) {
            list.clear();
        }

        lastWidth = 0;
        lastVelocity = 0;

        mBitmap.recycle();
        mBitmap = Bitmap.createBitmap(mBitmap.getWidth(), mBitmap.getHeight(), Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mBitmap);

        cache.clear();

        mCanvas.drawColor(color, PorterDuff.Mode.CLEAR);

        canDraw = true;
    }

    public void drawBitmapToCanvas(Canvas canvas) {
        if (canDraw) {
            canvas.drawBitmap(mBitmap, 0, 0, null);
        }
    }

    public void onTouchUp(int pointer) {
        historyCounter++;
        cache.set(String.valueOf(historyCounter), mBitmap);

        lastVelocity = 0;
        lastWidth = 0;

        pointerPoints[pointer].clear();
    }

    public void addPoint(float x, float y, int pointerId) {
        DrawingPoint prevPoint;
        DrawingPoint nextPoint = new DrawingPoint(x, y, SystemClock.currentThreadTimeMillis(), mPaint.getStrokeWidth());

        ArrayList<DrawingPoint> points = pointerPoints[pointerId];
        if (points.isEmpty()) {
            mCanvas.drawPoint(x, y, mPaint);
            points.add(nextPoint);
        } else {
            prevPoint = points.get(points.size() - 1);

            if (Math.abs(prevPoint.x - x) < POINT_TOLERANCE && Math.abs(prevPoint.y - y) < POINT_TOLERANCE) {
                return;
            }

            if (mState == State.ERASE) {
                mCanvas.drawLine(prevPoint.x, prevPoint.y, nextPoint.x, nextPoint.y, mPaint);
                return;
            }

            points.add(nextPoint);

            algorithmDraw(prevPoint, nextPoint, points);
        }
    }

    private void algorithmDraw(DrawingPoint previous, DrawingPoint current, ArrayList<DrawingPoint> points) {
        DrawingPoint mid = current.midPoint(previous);

        float velocity =  VELOCITY_FILTER_WEIGHT * current.velocityFrom(previous)
                + (1 - VELOCITY_FILTER_WEIGHT) * lastVelocity;
        float strokeWidth = Math.abs(STROKE_WIDTH - velocity);
        if (strokeWidth < POINT_MIN_WIDTH) { strokeWidth = POINT_MIN_WIDTH; }
        if (strokeWidth > POINT_MAX_WIDTH) { strokeWidth = POINT_MAX_WIDTH; }
        float diff = strokeWidth - lastWidth;
        float lastX = current.x, lastY = current.y;

        float xa, xb, ya, yb, x, y;
        for (float i = 0; i < 1; i += .03) {
            xa = previous.x + (previous.x - mid.x) * i;
            ya = previous.y + (previous.y - mid.y) * i;

            xb = mid.x + (current.x - mid.x) * i;
            yb = mid.y + (current.y - mid.y) * i;

            x = xa + ((xb - xa) * i);
            y = ya + ((yb - ya) * i);

            float width = lastWidth + diff * i;
            mPaint.setStrokeWidth(width);

            points.add(new DrawingPoint(x, y, (previous.time + current.time) / 2, width));
            mCanvas.drawLine(lastX, lastY, x, y, mPaint);

            lastX = x;
            lastY = y;
        }

        lastWidth = strokeWidth;
        lastVelocity = velocity;
    }

    public boolean redo() {
        Logg.log("FOR REDO: " + historyCounter);
        Bitmap test = cache.get(String.valueOf(historyCounter));
        if (test != null) {
            canDraw = false;

            historyCounter++;

            mBitmap = Bitmap.createBitmap(test);
            mCanvas = new Canvas(mBitmap);

            canDraw = true;

            return true;
        }

        return false;
    }

    public boolean undo() {
        Logg.log("FOR UNDO: " + historyCounter);

        if (historyCounter > 0) {
            canDraw = false;

            historyCounter--;

            Bitmap test = cache.get(String.valueOf(historyCounter));

            Logg.log(test == mBitmap);

            mBitmap = Bitmap.createBitmap(test);
            mCanvas = new Canvas(mBitmap);

            canDraw = true;

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
