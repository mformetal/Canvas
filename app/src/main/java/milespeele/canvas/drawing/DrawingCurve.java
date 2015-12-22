package milespeele.canvas.drawing;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.SystemClock;
import android.text.DynamicLayout;
import android.text.Layout;
import android.text.TextPaint;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import java.util.ArrayList;
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
    private Bitmap mBitmap, cachedBitmap;
    private Canvas mCanvas;
    private DrawingPoints currentPoints;
    private DrawingHistory redoneHistory, allHistory;
    private DrawingPaint mPaint;
    private TextPaint textSerializablePaint;
    private State mState = State.DRAW;
    private FileUtils fileUtils;

    private static final float TOLERANCE = 5f;
    private static float STROKE_WIDTH = 5f;
    private int activePointer = 0;
    private float lastX, lastY;
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

        currentStrokeColor = ViewUtils.randomColor();
        currentBackgroundColor = store.getLastBackgroundColor();
        oppositeBackgroundColor = ViewUtils.getComplimentColor(currentBackgroundColor);
        inkedColor = currentStrokeColor;

        cachedBitmap = fileUtils.getCachedBitmap();
        if (cachedBitmap == null) {
            cachedBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
            cachedBitmap.eraseColor(currentBackgroundColor);
        }

        mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mBitmap);
        mCanvas.drawBitmap(cachedBitmap, 0, 0, null);

        mPaint = new DrawingPaint(PaintStyles.normal(currentStrokeColor, 10f));

        textSerializablePaint = new TextPaint(TextPaint.ANTI_ALIAS_FLAG);
        textSerializablePaint.setColor(currentStrokeColor);
        textSerializablePaint.setStyle(Paint.Style.FILL_AND_STROKE);

        scaleGestureDetector = new ScaleGestureDetector(context, new ScaleListener());

        allHistory = new DrawingHistory();
        redoneHistory = new DrawingHistory();
        currentPoints = new DrawingPoints(mPaint);
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

    private void changeState(State newValue) {
        mState = newValue;

        switch (mState) {
            case ERASE:
                setPaintColor(currentBackgroundColor);
                setPaintThickness(20f);
                break;
            case DRAW:
                setPaintColor(currentStrokeColor);
                setPaintThickness(STROKE_WIDTH);
                break;
            case RAINBOW:

                break;
        }
    }

    private void reset(int color) {
        isSafeToDraw = false;
        int width = mBitmap.getWidth(), height = mBitmap.getHeight();

        fileUtils.deleteBitmapFile();

        currentPoints.clear();
        allHistory.clear();
        redoneHistory.clear();

        cachedBitmap.recycle();
        cachedBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        cachedBitmap.eraseColor(color);

        mBitmap.recycle();
        mBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mBitmap);

        ValueAnimator colorAnimation = ObjectAnimator.ofArgb(currentBackgroundColor, color);
        colorAnimation.addUpdateListener(animator ->
                mCanvas.drawColor((Integer) animator.getAnimatedValue()));
        colorAnimation.setDuration(1000);
        colorAnimation.start();

        currentBackgroundColor = color;
        oppositeBackgroundColor = ViewUtils.getComplimentColor(currentBackgroundColor);

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
                currentPoints.storePoints();
                allHistory.push(currentPoints);
                currentPoints.clear();
                break;
            case TEXT:
                break;
            case INK:
                if (currentBackgroundColor != inkedColor) {
                    currentStrokeColor = inkedColor;
                    mPaint.setColor(currentStrokeColor);
                }
                changeState(State.DRAW);
                break;
        }

        lastX = event.getX();
        lastY = event.getY();
    }

    private void onPointerUp(MotionEvent event) {
        int pointerIndex = (event.getAction() & MotionEvent.ACTION_POINTER_INDEX_MASK)
                >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
        int pointerId = event.getPointerId(pointerIndex);
        if (pointerId == activePointer) {
            final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
            lastX = event.getX(newPointerIndex);
            lastY = event.getY(newPointerIndex);
            activePointer = event.getPointerId(newPointerIndex);
        }
    }

    private void addPoint(float x, float y) {
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

            if (Math.abs(prevPoint.x - x) < TOLERANCE &&
                    Math.abs(prevPoint.y - y) < TOLERANCE) {
                return;
            }

            nextPoint = new DrawingPoint(x, y, SystemClock.currentThreadTimeMillis(),
                    mPaint.getStrokeWidth(), mPaint.getColor());

            currentPoints.add(nextPoint);
            algorithmDraw(prevPoint, nextPoint);
        }
    }

    private void algorithmDraw(DrawingPoint previous, DrawingPoint current) {
        mCanvas.drawLine(previous.x, previous.y, current.x, current.y, mPaint);
    }

    public boolean redo() {
        if (!redoneHistory.isEmpty()) {
            isSafeToDraw = false;
            long start = SystemClock.currentThreadTimeMillis();

            allHistory.push(redoneHistory.pop());

            mCanvas.drawBitmap(cachedBitmap, 0, 0, null);

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

            mCanvas.drawBitmap(cachedBitmap, 0, 0, null);

            allHistory.redraw(mCanvas);

            Logg.log("ELAPSED: " + (SystemClock.currentThreadTimeMillis() - start) / 1000.0);

            isSafeToDraw = true;
            return isSafeToDraw;
        }
        return false;
    }

    public boolean ink() {
        if (mState == State.INK) {
            changeState(State.DRAW);
            return false;
        } else {
            changeState(State.DRAW);
            translateY = 0;
            translateX = 0;

            changeState(State.INK);

            inkedColor = mBitmap.getPixel(mBitmap.getWidth() / 2, mBitmap.getHeight() / 2);
            return false;
        }
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
        int color = eventColorChosen.color;
        if (eventColorChosen.bool) {
            reset(color);

            changeState(State.DRAW);
        } else {
            changeState(State.DRAW);

            setPaintColor(color);
        }
    }

    public void onEvent(EventBrushChosen eventBrushChosen) {
        Paint newSerializablePaint = eventBrushChosen.paint;

        changeState(State.DRAW);

        mPaint.set(newSerializablePaint);
        mPaint.setColor(currentStrokeColor);
        currentPoints.redrawPaint.set(mPaint);

        setPaintThickness(newSerializablePaint.getStrokeWidth());
    }

    public int getStrokeColor() { return currentStrokeColor; }

    public Bitmap getBitmap() { return mBitmap; }

    public Paint getPaint() { return mPaint; }

    private void setPaintColor(int color) {
        textSerializablePaint.setColor(color);
        mPaint.setColor(color);
        currentPoints.redrawPaint.setColor(color);
    }

    private void setPaintThickness(float floater) {
        STROKE_WIDTH = mPaint.getStrokeWidth();
        textSerializablePaint.setStrokeWidth(floater);
        mPaint.setStrokeWidth(floater);
        currentPoints.redrawPaint.setStrokeWidth(floater);
    }

    private boolean eventCoordsInRange(int x, int y) {
        return (0 <= x && x <= mBitmap.getWidth() - 1) &&
                (0 <= y && y <= mBitmap.getHeight() - 1);
    }

    public void onSave() {
        store.setLastBackgroundColor(currentBackgroundColor);
        fileUtils.cacheBitmap(mBitmap);
    }

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
