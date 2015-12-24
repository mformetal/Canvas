package milespeele.canvas.drawing;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.SystemClock;
import android.text.DynamicLayout;
import android.text.Layout;
import android.text.TextPaint;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;

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
        TEXT,
        INK
    }

    private DynamicLayout mTextLayout;
    private ScaleGestureDetector mGestureDetector;
    private Bitmap mBitmap, mCachedBitmap;
    private Canvas mCanvas;
    private DrawingPoints mCurrentPoints;
    private DrawingHistory mRedoneHistory, mAllHistory;
    private Paint mPaint, mInkPaint;
    private TextPaint mTextPaint;
    private State mState = State.DRAW;
    private FileUtils mFileUtils;

    private static final float TOLERANCE = 5f;
    private static float STROKE_WIDTH = 5f;
    private static final int INVALID_POINTER = -1;
    private float mScaleFactor = 1f;
    private int mActivePointer = INVALID_POINTER;
    private float mLastX, mLastY;
    private float mTranslateX, mTranslateY;
    private float cx, cy;
    private int mStrokeColor, mBackgroundColor, mOppositeBackgroundColor, mInkedColor;
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

        mFileUtils = new FileUtils(context);

        mStrokeColor = ViewUtils.randomColor();
        mBackgroundColor = store.getLastBackgroundColor();
        mOppositeBackgroundColor = ViewUtils.getComplimentColor(mBackgroundColor);
        mInkedColor = mStrokeColor;

        mCachedBitmap = mFileUtils.getCachedBitmap();
        if (mCachedBitmap == null) {
            mCachedBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
            mCachedBitmap.eraseColor(mBackgroundColor);
        }

        mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mBitmap);
        mCanvas.drawBitmap(mCachedBitmap, 0, 0, null);

        mPaint = PaintStyles.normal(mStrokeColor, 10f);
        mInkPaint = PaintStyles.normal(mStrokeColor, 10f);

        mTextPaint = new TextPaint(TextPaint.ANTI_ALIAS_FLAG);
        mTextPaint.setColor(mStrokeColor);
        mTextPaint.setStyle(Paint.Style.FILL_AND_STROKE);

        mGestureDetector = new ScaleGestureDetector(context, new ScaleListener());

        mAllHistory = new DrawingHistory();
        mRedoneHistory = new DrawingHistory();
        mCurrentPoints = new DrawingPoints(mPaint);
    }

    public void onButtonClicked() {
        switch (mState) {
            case TEXT:
                listener.hideButton();

                mCanvas.save();
                mCanvas.translate(mTranslateX, mTranslateY);
                mCanvas.scale(mScaleFactor, mScaleFactor);
                mTextLayout.draw(mCanvas);
                mCanvas.restore();

//                mAllHistory.push(new DrawingText(mTextLayout.getText(), mLastX, mLastY, scaleFactor, mTextPaint));

                changeState(State.DRAW);

                mTranslateX = 0;
                mTranslateY = 0;

                mTextLayout = null;
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
                    canvas.translate(mTranslateX, mTranslateY);
                    canvas.scale(mScaleFactor, mScaleFactor, cx, cy);
//                    canvas.rotate(mRotateAngle, mBitmap.getWidth() / 2f, mBitmap.getHeight() / 2f);
                    mTextLayout.draw(canvas);
                    canvas.restore();
                    break;
                case INK:
                    mInkPaint.setStrokeWidth(20f);

                    canvas.save();
                    canvas.translate(mTranslateX, mTranslateY);

                    float lineSize = canvas.getWidth() * .1f, xSpace = canvas.getWidth() * .05f;
                    float middleX = canvas.getWidth() / 2f, middleY = canvas.getHeight() / 2f;

                    // base "pointer"
                    mInkPaint.setColor(mOppositeBackgroundColor);
                    canvas.drawLine(middleX + xSpace / 2, middleY, middleX + xSpace + lineSize, middleY, mInkPaint);
                    canvas.drawLine(middleX - xSpace / 2, middleY, middleX - xSpace - lineSize, middleY, mInkPaint);
                    canvas.drawLine(middleX, middleY + xSpace / 2, middleX, middleY + xSpace + lineSize, mInkPaint);
                    canvas.drawLine(middleX, middleY - xSpace / 2, middleX, middleY - xSpace - lineSize, mInkPaint);
                    canvas.drawCircle(middleX, middleY, xSpace + lineSize, mInkPaint);

                    // ink circle
                    mInkPaint.setColor(mInkedColor);
                    canvas.drawCircle(middleX, middleY, xSpace + lineSize - mPaint.getStrokeWidth(), mInkPaint);

                    canvas.restore();
                    break;
            }
        }
    }

    private void changeState(State newValue) {
        mState = newValue;

        switch (mState) {
            case ERASE:
                setPaintColor(mBackgroundColor);
                setPaintThickness(20f);
                break;
            case DRAW:
                setPaintColor(mStrokeColor);
                setPaintThickness(STROKE_WIDTH);
                break;
        }
    }

    private void reset(int color) {
        isSafeToDraw = false;
        int width = mBitmap.getWidth(), height = mBitmap.getHeight();

        mFileUtils.deleteBitmapFile();

        mCurrentPoints.clear();
        mAllHistory.clear();
        mRedoneHistory.clear();

        mCachedBitmap.recycle();
        mCachedBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        mCachedBitmap.eraseColor(color);

        mBitmap.recycle();
        mBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mBitmap);

        ValueAnimator colorAnimation = ObjectAnimator.ofArgb(mBackgroundColor, color);
        colorAnimation.addUpdateListener(animator ->
                mCanvas.drawColor((Integer) animator.getAnimatedValue()));
        colorAnimation.setDuration(1000);
        colorAnimation.start();

        mBackgroundColor = color;
        mOppositeBackgroundColor = ViewUtils.getComplimentColor(mBackgroundColor);

        isSafeToDraw = true;
    }

    public boolean onTouchEvent(MotionEvent event) {
        onTouchStart(event);

        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                onTouchDown(event);
                break;

            case MotionEvent.ACTION_POINTER_DOWN:
                onPointerDown(event);
                break;

            case MotionEvent.ACTION_MOVE:
                onTouchMove(event);
                break;

            case MotionEvent.ACTION_UP:
                onTouchUp(event);
                break;

            case MotionEvent.ACTION_POINTER_UP:
                onPointerUp(event);
                break;

            case MotionEvent.ACTION_CANCEL:
                onCancel(event);
                break;
        }

        return true;
    }

    private void onTouchStart(MotionEvent event) {
        switch (mState) {
            case TEXT:
                mGestureDetector.onTouchEvent(event);
                break;
        }
    }

    private void onTouchDown(MotionEvent event) {
        float x = event.getX(), y = event.getY();

        switch (mState) {
            case ERASE:
            case DRAW:
                break;
            case TEXT:
                break;
            case INK:
                mTranslateX = x - mBitmap.getWidth() / 2f;
                mTranslateY = y - mBitmap.getHeight() / 2f - mBitmap.getWidth() * .15f;

                int inkX = Math.round(x), inkY = Math.round(y);
                if (eventCoordsInRange(inkX, inkY)) {
                    mInkedColor = mBitmap.getPixel(inkX, inkY);
                }
                break;
        }

        mActivePointer = event.getPointerId(0);
        mLastX = event.getX();
        mLastY = event.getY();
    }

    public void onPointerDown(MotionEvent event) {
        switch (mState) {
            case ERASE:
                break;
            case DRAW:
                break;
            case TEXT:
                break;
            case INK:
                break;
        }
    }

    private void onTouchMove(MotionEvent event) {
        final int pointerIndex = event.findPointerIndex(mActivePointer);
        float x = event.getX(pointerIndex), y = event.getY(pointerIndex);

        switch (mState) {
            case ERASE:
            case DRAW:
                for (int i = 0; i < event.getHistorySize(); i++) {
                    addPoint(event.getHistoricalX(pointerIndex, i),
                            event.getHistoricalY(pointerIndex, i));
                }
                addPoint(x, y);
                break;
            case TEXT:
                mTranslateX += x - mLastX;
                mTranslateY += y - mLastY;

                if (event.getPointerCount() > 1) {
                    int pointer = mActivePointer == 0 ? 1 : 0;
                    int index = event.findPointerIndex(pointer);
                    float nx = event.getX(index);
                    float ny = event.getY(index);

                    cx = (x + nx) / 2f;
                    cy = (y + ny) / 2f;
                }
                break;
            case INK:
                mTranslateX += x - mLastX;
                mTranslateY += y - mLastY;

                int inkX = Math.round(x), inkY = Math.round(y - mBitmap.getWidth() * .15f);
                if (eventCoordsInRange(inkX, inkY)) {
                    mInkedColor = mBitmap.getPixel(inkX, inkY);
                    mStrokeColor = mInkedColor;
                    setPaintColor(mStrokeColor);
                }
                break;
        }

        mLastX = x;
        mLastY = y;
    }

    private void onTouchUp(MotionEvent event) {
        mActivePointer = -1;

        switch (mState) {
            case ERASE:
            case DRAW:
                mCurrentPoints.storePoints();
                mAllHistory.push(mCurrentPoints);
                mCurrentPoints.clear();
                break;
            case TEXT:
                break;
            case INK:
                mStrokeColor = mInkedColor;
                setPaintColor(mStrokeColor);
                changeState(State.DRAW);
                break;
        }

        mLastX = event.getX();
        mLastY = event.getY();
    }

    private void onCancel(MotionEvent event) {
        mActivePointer = -1;
    }

    private void onPointerUp(MotionEvent event) {
        int pointerIndex = (event.getAction() & MotionEvent.ACTION_POINTER_INDEX_MASK)
                >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
        int pointerId = event.getPointerId(pointerIndex);
        if (pointerId == mActivePointer) {
            final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
            mLastX = event.getX(newPointerIndex);
            mLastY = event.getY(newPointerIndex);
            mActivePointer = event.getPointerId(newPointerIndex);
        }
    }

    private void addPoint(float x, float y) {
        DrawingPoint nextPoint;
        if (mCurrentPoints.isEmpty()) {
            nextPoint = new DrawingPoint(x, y, SystemClock.currentThreadTimeMillis(),
                    mPaint.getStrokeWidth(), mPaint.getColor());

            mPaint.setStrokeWidth(mPaint.getStrokeWidth() / 2);
            mCanvas.drawPoint(x, y, mPaint);
            mCurrentPoints.add(nextPoint);
            mPaint.setStrokeWidth(mPaint.getStrokeWidth() * 2);
        } else {
            DrawingPoint prevPoint = mCurrentPoints.peek();

            if (Math.abs(prevPoint.x - x) < TOLERANCE &&
                    Math.abs(prevPoint.y - y) < TOLERANCE) {
                return;
            }

            nextPoint = new DrawingPoint(x, y, SystemClock.currentThreadTimeMillis(),
                    mPaint.getStrokeWidth(), mPaint.getColor());

            mCurrentPoints.add(nextPoint);
            algorithmDraw(prevPoint, nextPoint);
        }
    }

    private void algorithmDraw(DrawingPoint previous, DrawingPoint current) {
        mCanvas.drawLine(previous.x, previous.y, current.x, current.y, mPaint);
    }

    public boolean redo() {
        if (!mRedoneHistory.isEmpty()) {
            isSafeToDraw = false;
            long start = SystemClock.currentThreadTimeMillis();

            mAllHistory.push(mRedoneHistory.pop());

            mCanvas.drawBitmap(mCachedBitmap, 0, 0, null);

            mAllHistory.redraw(mCanvas);

            Logg.log("ELAPSED: " + (SystemClock.currentThreadTimeMillis() - start) / 1000.0);
            isSafeToDraw = true;
            return isSafeToDraw;
        }
        return false;
    }

    public boolean undo() {
        if (!mAllHistory.isEmpty()) {
            isSafeToDraw = false;

            long start = SystemClock.currentThreadTimeMillis();

            mRedoneHistory.push(mAllHistory.pop());

            mCanvas.drawBitmap(mCachedBitmap, 0, 0, null);

            mAllHistory.redraw(mCanvas);

            Logg.log("ELAPSED: " + (SystemClock.currentThreadTimeMillis() - start) / 1000.0);

            isSafeToDraw = true;
            return isSafeToDraw;
        }
        return false;
    }

    public void ink() {
        mTranslateY = 0;
        mTranslateX = 0;

        changeState(State.INK);

        mInkedColor = mBitmap.getPixel(mBitmap.getWidth() / 2, mBitmap.getHeight() / 2);
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
        Rect bounds = new Rect();
        mTextPaint.getTextBounds(textToBeDrawn, 0, textToBeDrawn.length(), bounds);
        mTextPaint.setColor(mStrokeColor);

        TextUtils.adjustTextSize(mTextPaint, textToBeDrawn, height);
        TextUtils.adjustTextScale(mTextPaint, textToBeDrawn, width, 0, 0);

        mTextLayout = new DynamicLayout(textToBeDrawn, mTextPaint, mBitmap.getWidth(),
                Layout.Alignment.ALIGN_CENTER, 0, 0, false);

        changeState(State.TEXT);

        mTranslateX = 0;
        mTranslateY = height / 2f - mTextLayout.getHeight() / 2;

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
        Paint paint = eventBrushChosen.paint;

        changeState(State.DRAW);

        mPaint.set(paint);
        mPaint.setColor(mStrokeColor);
        mCurrentPoints.redrawPaint.set(mPaint);

        setPaintThickness(paint.getStrokeWidth());
    }

    public int getStrokeColor() { return mStrokeColor; }

    public Bitmap getBitmap() { return mBitmap; }

    public Paint getPaint() { return mPaint; }

    private void setPaintColor(int color) {
        mTextPaint.setColor(color);
        mPaint.setColor(color);
        mCurrentPoints.redrawPaint.setColor(color);
    }

    private void setPaintThickness(float floater) {
        mTextPaint.setStrokeWidth(floater);
        mPaint.setStrokeWidth(floater);
        mCurrentPoints.redrawPaint.setStrokeWidth(floater);
    }

    private boolean eventCoordsInRange(int x, int y) {
        return (0 <= x && x <= mBitmap.getWidth() - 1) &&
                (0 <= y && y <= mBitmap.getHeight() - 1);
    }

    public void onSave() {
        store.setLastBackgroundColor(mBackgroundColor);
        mFileUtils.cacheBitmap(mBitmap);
    }

    private final class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {

        private static final float MAX_SCALE = 5.0f, MIN_SCALE = .1f;

        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {
            return true;
        }

        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            mScaleFactor *= detector.getScaleFactor();
            mScaleFactor = Math.max(MIN_SCALE, Math.min(mScaleFactor, MAX_SCALE));
            return true;
        }

        @Override
        public void onScaleEnd(ScaleGestureDetector detector) {
            super.onScaleEnd(detector);
        }
    }
}
