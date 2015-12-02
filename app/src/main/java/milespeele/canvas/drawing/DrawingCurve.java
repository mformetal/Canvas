package milespeele.canvas.drawing;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.os.SystemClock;
import android.text.DynamicLayout;
import android.text.Layout;
import android.text.TextPaint;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;

import java.util.Random;

import javax.inject.Inject;

import de.greenrobot.event.EventBus;
import milespeele.canvas.MainApp;
import milespeele.canvas.event.EventBrushChosen;
import milespeele.canvas.event.EventColorChosen;
import milespeele.canvas.event.EventTextChosen;
import milespeele.canvas.util.FileUtils;
import milespeele.canvas.util.Datastore;
import milespeele.canvas.util.Logg;
import milespeele.canvas.util.PaintStyles;
import milespeele.canvas.util.TextUtils;
import milespeele.canvas.util.ViewUtils;

/**
 * Created by mbpeele on 9/25/15.
 */
public class DrawingCurve {

    public enum State {
        DRAW,
        ERASE,
        RAINBOW,
        TEXT,
        INK
    }

    private DynamicLayout textLayout;
    private ScaleGestureDetector scaleGestureDetector;
    private Bitmap mBitmap;
    private Canvas mCanvas;
    private DrawingPoints currentPoints;
    private DrawingHistory redoneHistory, allHistory;
    private Paint mPaint;
    private TextPaint textSerializablePaint;
    private Random random;
    private State mState = State.DRAW;
    private FileUtils fileUtils;

    private static final float VELOCITY_FILTER_WEIGHT = 0.2f;
    private static float STROKE_WIDTH = 10f;
    private static final float POINT_MAX_WIDTH = 50f, POINT_MIN_WIDTH = 2f, POINT_TOLERANCE = 5f;
    private int[] rainbow;
    private int activePointer = 0;
    private float lastX, lastY;
    private float lastWidth, lastVelocity;
    private float translateX, translateY;
    private int currentStrokeColor, currentBackgroundColor, oppositeBackgroundColor, inkedColor;
    private boolean isSafeToDraw = true;

    @Inject Datastore store;
    @Inject EventBus bus;

    private DrawingCurveListener listener;
    public interface DrawingCurveListener {
        void showButton(String buttonText);
        void hideButton();
    }

    public DrawingCurve(Context context, int w, int h) {
        ((MainApp) context.getApplicationContext()).getApplicationComponent().inject(this);
        bus.register(this);

        fileUtils = new FileUtils(context);

        random = new Random();

        rainbow = ViewUtils.rainbow();

        currentStrokeColor = Color.argb(255, random.nextInt(256), random.nextInt(256), random.nextInt(256));
        currentBackgroundColor = store.getLastBackgroundColor();
        oppositeBackgroundColor = ViewUtils.getComplementaryColor(currentBackgroundColor);
        inkedColor = currentStrokeColor;

        Bitmap cachedBitmap = fileUtils.getCachedBitmap();
        if (cachedBitmap == null) {
            cachedBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
            cachedBitmap.eraseColor(currentBackgroundColor);
        }

        mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mBitmap);
        mCanvas.drawBitmap(cachedBitmap, 0, 0, null);

        mPaint = new Paint(PaintStyles.normal(currentStrokeColor, STROKE_WIDTH));

        textSerializablePaint = new TextPaint(TextPaint.ANTI_ALIAS_FLAG);
        textSerializablePaint.setColor(currentStrokeColor);
        textSerializablePaint.setStyle(Paint.Style.FILL_AND_STROKE);

        scaleGestureDetector = new ScaleGestureDetector(context, new ScaleListener());

        allHistory = fileUtils.getAllHistory();
        redoneHistory = fileUtils.getRedoneHistory();
        currentPoints = new DrawingPoints();
    }

    public void onButtonClicked() {
        switch (mState) {
            case TEXT:
                listener.hideButton();

                mCanvas.save();
                mCanvas.translate(translateX, translateY);
                textLayout.draw(mCanvas);
                mCanvas.restore();

//                allHistory.push(new DrawingText(textLayout.getText(), lastX, lastY, scaleFactor, textSerializablePaint));

                changeState(State.DRAW);

                translateX = 0;
                translateY = 0;

                textLayout = null;
                break;
        }
    }

    public void setListener(DrawingCurveListener listener) {
        this.listener = listener;
    }

    public void drawToSurfaceView(Canvas canvas) {
        if (isSafeToDraw && mBitmap != null && canvas != null) {
            canvas.drawBitmap(mBitmap, 0, 0, null);

            switch (mState) {
                case TEXT:
                    canvas.save();
                    canvas.translate(translateX, translateY);
                    textLayout.draw(canvas);
                    canvas.restore();
                    break;
                case INK:
                    int prevColor = mPaint.getColor();
                    float prevWidth = mPaint.getStrokeWidth();
                    mPaint.setStrokeWidth(20f);

                    canvas.save();
                    canvas.translate(translateX, translateY);

                    float lineSize = canvas.getWidth() * .1f, xSpace = canvas.getWidth() * .05f;
                    float middleX = canvas.getWidth() / 2f, middleY = canvas.getHeight() / 2f;

                    // base "pointer"
                    mPaint.setColor(oppositeBackgroundColor);
                    canvas.drawLine(middleX + xSpace, middleY, middleX + xSpace + lineSize, middleY, mPaint);
                    canvas.drawLine(middleX - xSpace, middleY, middleX - xSpace - lineSize, middleY, mPaint);
                    canvas.drawLine(middleX, middleY + xSpace, middleX, middleY + xSpace + lineSize, mPaint);
                    canvas.drawLine(middleX, middleY - xSpace, middleX, middleY - xSpace - lineSize, mPaint);
                    canvas.drawCircle(middleX, middleY, xSpace + lineSize, mPaint);

                    // ink circle
                    mPaint.setColor(inkedColor);
                    canvas.drawCircle(middleX, middleY, xSpace + lineSize - mPaint.getStrokeWidth(), mPaint);
                    canvas.restore();

                    mPaint.setColor(prevColor);
                    mPaint.setStrokeWidth(prevWidth);
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
                setSerializablePaintThickness(20f);
                break;
            case DRAW:
                mHandler.removeCallbacksAndMessages(null);
                mPaint.setColor(currentStrokeColor);
                mPaint.setStrokeWidth(STROKE_WIDTH);
                break;
            case RAINBOW:
                SerializablePaintRunnable.run();
                break;
        }
    }

    public void reset(int color) {
        isSafeToDraw = false;
        int width = mBitmap.getWidth(), height = mBitmap.getHeight();


        fileUtils.deleteAllHistoryFile();
        fileUtils.deleteRedoneHistoryFile();
        fileUtils.deleteBitmapFile();

        currentPoints.clear();
        allHistory.clear();
        redoneHistory.clear();

        mBitmap.recycle();
        mBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mBitmap);

        ValueAnimator colorAnimation = ValueAnimator.ofObject(
                new ArgbEvaluator(), currentBackgroundColor, color);
        colorAnimation.addUpdateListener(animator ->
                mCanvas.drawColor((Integer) animator.getAnimatedValue()));
        colorAnimation.setDuration(1000);
        colorAnimation.start();

        currentBackgroundColor = color;
        oppositeBackgroundColor = ViewUtils.getComplementaryColor(currentBackgroundColor);

        isSafeToDraw = true;
    }

    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();

        onTouchStart(event);

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
                onPointerUp(event);
                break;
        }

        return true;
    }

    private void onTouchStart(MotionEvent event) {
        switch (mState) {
            case TEXT:
                scaleGestureDetector.onTouchEvent(event);
                break;
        }
    }

    private void onTouchDown(MotionEvent event) {
        float x = event.getX(), y = event.getY();

        switch (mState) {
            case ERASE:
            case DRAW:
            case RAINBOW:
                addPoint(x, y);
                break;
            case TEXT:
                translateX = x - mBitmap.getWidth() / 2f;
                translateY = y - mBitmap.getHeight() / 2f;
                break;
            case INK:
                translateX = x - mBitmap.getWidth() / 2f;
                translateY = y - mBitmap.getHeight() / 2f - mBitmap.getWidth() * .15f;

                int inkX = Math.round(x), inkY = Math.round(y);
                if (eventCoordsInRange(inkX, inkY)) {
                    inkedColor = mBitmap.getPixel(inkX, inkY);
                }
                break;
        }

        activePointer = event.getPointerId(0);
        lastX = event.getX();
        lastY = event.getY();
    }

    private void onTouchMove(MotionEvent event) {
        int pointerIndex = event.findPointerIndex(activePointer);
        float x = event.getX(pointerIndex), y = event.getY(pointerIndex);

        switch (mState) {
            case ERASE:
            case DRAW:
            case RAINBOW:
                for (int i = 0; i < event.getHistorySize(); i++) {
                    addPoint(event.getHistoricalX(pointerIndex, i),
                            event.getHistoricalY(pointerIndex, i));
                }
                addPoint(x, y);
                break;
            case TEXT:
                translateX += x - lastX;
                translateY += y - lastY;
                break;
            case INK:
                translateX += x - lastX;
                translateY += y - lastY;

                int inkX = Math.round(x), inkY = Math.round(y - mBitmap.getWidth() * .15f);
                if (eventCoordsInRange(inkX, inkY)) {
                    inkedColor = mBitmap.getPixel(inkX, inkY);
                }
                break;
        }

        lastX = x;
        lastY = y;
    }

    private void onTouchUp(MotionEvent event) {
        activePointer = -1;

        switch (mState) {
            case ERASE:
            case DRAW:
            case RAINBOW:
                allHistory.push(currentPoints);
                currentPoints.clear();
                break;
            case TEXT:
                break;
            case INK:
                currentStrokeColor = inkedColor;
                mPaint.setColor(currentStrokeColor);
                changeState(State.DRAW);
                break;
        }

        lastX = event.getX();
        lastY = event.getY();
    }

    private void onPointerUp(MotionEvent event) {
        // Extract the index of the pointer that left the touch sensor
        currentPoints.clear();
        allHistory.push(currentPoints);

        final int pointerIndex = (event.getAction() & MotionEvent.ACTION_POINTER_INDEX_MASK)
                >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
        final int pointerId = event.getPointerId(pointerIndex);
        if (pointerId == activePointer) {
            // This was our active pointer going up. Choose a new
            // active pointer and adjust accordingly.
            final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
            lastX = event.getX(newPointerIndex);
            lastY = event.getY(newPointerIndex);
            activePointer = event.getPointerId(newPointerIndex);
        }
    }

    public void addPoint(float x, float y) {
        DrawingPoint nextPoint;
        if (currentPoints.isEmpty()) {
            nextPoint = new DrawingPoint(x, y, SystemClock.currentThreadTimeMillis(),
                    mPaint.getStrokeWidth(), mPaint.getColor());

            mPaint.setStrokeWidth(mPaint.getStrokeWidth() / 2);
            mCanvas.drawPoint(x, y, mPaint);
            currentPoints.add(nextPoint);
            mPaint.setStrokeWidth(mPaint.getStrokeWidth() * 2);
        } else {
            DrawingPoint prevPoint = currentPoints.peek();

            if (Math.abs(prevPoint.x - x) < POINT_TOLERANCE &&
                    Math.abs(prevPoint.y - y) < POINT_TOLERANCE) {
                return;
            }

            nextPoint = new DrawingPoint(x, y, SystemClock.currentThreadTimeMillis(),
                    mPaint.getStrokeWidth(), mPaint.getColor());

            currentPoints.add(nextPoint);
            algorithmDraw(prevPoint, nextPoint);
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

        float xa, xb, ya, yb, x, y;
        for (float i = 0; i < 1; i += (mPaint.getStrokeWidth() > 40f) ? .1 : .02) {
            xa = previous.x + (previous.x - mid.x) * i;
            ya = previous.y + (previous.y - mid.y) * i;

            xb = mid.x + (current.x - mid.x) * i;
            yb = mid.y + (current.y - mid.y) * i;

            x = xa + ((xb - xa) * i);
            y = ya + ((yb - ya) * i);

            float width = lastWidth + diff * i;
            if (mState != State.ERASE) {
                mPaint.setStrokeWidth(width);
            }

            currentPoints.add(new DrawingPoint(x, y, mid.time, mPaint.getStrokeWidth(), mPaint.getColor()));
            mCanvas.drawPoint(x, y, mPaint);
        }

        lastWidth = strokeWidth;
        lastVelocity = velocity;
    }

    public boolean redo() {
        if (!redoneHistory.isEmpty()) {
            isSafeToDraw = false;
            long start = SystemClock.currentThreadTimeMillis();

            allHistory.push(redoneHistory.pop());

            mCanvas.drawColor(currentBackgroundColor);

            allHistory.redraw(mCanvas);

            Logg.log("ELAPSED: " + (SystemClock.currentThreadTimeMillis() - start) / 1000.0);
            isSafeToDraw = true;

            return isSafeToDraw;
        }
        return false;
    }

    public boolean undo() {
        if (!allHistory.isEmpty()) {
            isSafeToDraw = false;
            long start = SystemClock.currentThreadTimeMillis();

            redoneHistory.push(allHistory.pop());

            mCanvas.drawColor(currentBackgroundColor);

            allHistory.redraw(mCanvas);

            Logg.log("ELAPSED: " + (SystemClock.currentThreadTimeMillis() - start) / 1000.0);
            isSafeToDraw = true;

            return isSafeToDraw;
        }
        return false;
    }

    public void ink() {
        translateY = 0;
        translateX = 0;

        changeState(State.INK);

        inkedColor = mBitmap.getPixel(mBitmap.getWidth() / 2, mBitmap.getHeight() / 2);
    }

    public void erase() {
        if (mState == State.ERASE) {
            changeState(State.DRAW);
        } else {
            changeState(State.ERASE);
        }
    }

    public void onEvent(EventTextChosen eventTextChosen) {
        int width = mBitmap.getWidth(), height = mBitmap.getHeight();

        String textToBeDrawn = eventTextChosen.text;
        textSerializablePaint.setColor(currentStrokeColor);

        TextUtils.adjustTextSize(textSerializablePaint, textToBeDrawn, height);
        TextUtils.adjustTextScale(textSerializablePaint, textToBeDrawn, width, 0, 0);

        textLayout = new DynamicLayout(textToBeDrawn, textSerializablePaint, mBitmap.getWidth(),
                Layout.Alignment.ALIGN_CENTER, 0, 0, false);

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

                setSerializablePaintColor(eventColorChosen.color);
            }
        }
    }

    public void onEvent(EventBrushChosen eventBrushChosen) {
        Paint newSerializablePaint = eventBrushChosen.paint;

        if (newSerializablePaint.getShader() != null) {
            newSerializablePaint.setShader(null);
            changeState(State.RAINBOW);
        } else {
            changeState(State.DRAW);
        }

        int prevColor = mPaint.getColor();
        mPaint.set(newSerializablePaint);
        mPaint.setColor(prevColor);

        currentPoints.redrawPaint.set(mPaint);

        setSerializablePaintThickness(newSerializablePaint.getStrokeWidth());
    }

    public int getCurrentStrokeColor() { return currentStrokeColor; }

    public Bitmap getBitmap() { return mBitmap; }

    public Paint getCurrentSerializablePaint() { return mPaint; }

    public void setSerializablePaintColor(int color) {
        currentStrokeColor = color;
        textSerializablePaint.setColor(currentStrokeColor);
        mPaint.setColor(currentStrokeColor);

        currentPoints.redrawPaint.setColor(currentStrokeColor);
    }

    public void setSerializablePaintThickness(float floater) {
        STROKE_WIDTH = floater;
        textSerializablePaint.setStrokeWidth(STROKE_WIDTH);
        mPaint.setStrokeWidth(STROKE_WIDTH);

        currentPoints.redrawPaint.setStrokeWidth(STROKE_WIDTH);
    }

    private boolean eventCoordsInRange(int x, int y) {
        return (0 <= x && x <= mBitmap.getWidth() - 1) &&
                (0 <= y && y <= mBitmap.getHeight() - 1);
    }

    public void onSave() {
        store.setLastBackgroundColor(currentBackgroundColor);

        fileUtils.cacheAllHistory(allHistory);
        fileUtils.cacheRedoneHistory(redoneHistory);
        fileUtils.cacheBitmap(mBitmap);
    }

    private static final Handler mHandler = new Handler();
    private Runnable SerializablePaintRunnable = new Runnable() {
        public void run() {
            mPaint.setColor(rainbow[random.nextInt(rainbow.length)]);
            mHandler.postDelayed(this, 100);
        }
    };

    private final class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {

        private static final float MAX_SCALE = 5.0f, MIN_SCALE = .1f;

        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {
            return super.onScaleBegin(detector);
        }

        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            return true;
        }

        @Override
        public void onScaleEnd(ScaleGestureDetector detector) {
            super.onScaleEnd(detector);
        }
    }
}
