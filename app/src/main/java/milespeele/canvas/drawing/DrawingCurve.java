package milespeele.canvas.drawing;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.os.SystemClock;
import android.text.DynamicLayout;
import android.text.Layout;
import android.text.TextPaint;
import android.util.FloatMath;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import javax.inject.Inject;

import de.greenrobot.event.EventBus;
import milespeele.canvas.MainApp;
import milespeele.canvas.event.EventBitmapChosen;
import milespeele.canvas.event.EventBrushChosen;
import milespeele.canvas.event.EventColorChosen;
import milespeele.canvas.event.EventTextChosen;
import milespeele.canvas.util.FileUtils;
import milespeele.canvas.util.Datastore;
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
        INK,
        IMPORT
    }

    private DynamicLayout mTextLayout;
    private Matrix mMatrix;
    private ScaleGestureDetector mGestureDetector;
    private Bitmap mBitmap, mCachedBitmap, mPhotoBitmap;
    private Canvas mCanvas;
    private DrawingPoints mCurrentPoints;
    private DrawingHistory mRedoneHistory, mAllHistory;
    private Paint mPaint, mInkPaint;
    private TextPaint mTextPaint;
    private State mState = State.DRAW;
    private Context mContext;

    private static final float TOLERANCE = 5f;
    private static float STROKE_WIDTH = 5f;
    private static final int INVALID_POINTER = -1;
    private float[] mMatrixValues = new float[9];
    private int mActivePointer = INVALID_POINTER;
    private float mLastX, mLastY;
    private int mStrokeColor, mBackgroundColor, mOppositeBackgroundColor, mInkedColor;
    private boolean isSafeToDraw = true;

    // these matrices will be used to move and zoom image
    private Matrix matrix = new Matrix();
    private Matrix savedMatrix = new Matrix();
    // we can be in one of these 3 states
    private static final int NONE = 0;
    private static final int DRAG = 1;
    private static final int ZOOM = 2;
    private int mode = NONE;
    // remember some things for zooming
    private PointF start = new PointF();
    private PointF mid = new PointF();
    private float oldDist = 1f;
    private float d = 0f;
    private float newRot = 0f;
    private float[] lastEvent = null;

    @Inject Datastore store;
    @Inject EventBus bus;

    private DrawingCurveListener listener;
    public interface DrawingCurveListener {
        void toggleOptionsMenuVisibility(boolean visible);
        void toggleMenuVisibility(boolean visible);
    }

    public DrawingCurve(Context context) {
        ((MainApp) context.getApplicationContext()).getApplicationComponent().inject(this);
        bus.register(this);

        mContext = context;

        Point size = new Point();
        ((Activity) context).getWindowManager().getDefaultDisplay().getSize(size);
        int w = size.x;
        int h = size.y;

        mStrokeColor = ViewUtils.randomColor();
        mBackgroundColor = store.getLastBackgroundColor();
        mOppositeBackgroundColor = ViewUtils.getComplimentColor(mBackgroundColor);
        mInkedColor = mStrokeColor;

        mMatrix = new Matrix();

        mCachedBitmap = FileUtils.getCachedBitmap(mContext);
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
                listener.toggleOptionsMenuVisibility(false);

                mCanvas.save();
                mCanvas.concat(matrix);
                mTextLayout.draw(mCanvas);
                mCanvas.restore();

//                mAllHistory.push(new DrawingText(mTextLayout.getText(), mLastX, mLastY, scaleFactor, mTextPaint));

                changeState(State.DRAW);

                ViewUtils.setIdentityMatrix(mMatrix);

                mTextLayout = null;
                break;
            case IMPORT:
                listener.toggleOptionsMenuVisibility(false);

                mCanvas.save();
                mCanvas.concat(mMatrix);
                mCanvas.drawBitmap(mPhotoBitmap, 0, 0, null);
                mCanvas.restore();

                changeState(State.DRAW);

                ViewUtils.setIdentityMatrix(mMatrix);

                mPhotoBitmap = null;
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
                    canvas.concat(matrix);
                    mTextLayout.draw(canvas);
                    canvas.restore();
                    break;
                case INK:
                    mInkPaint.setStrokeWidth(20f);

                    canvas.save();
                    canvas.concat(mMatrix);

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
                case IMPORT:
                    canvas.save();
                    canvas.concat(mMatrix);
                    canvas.drawBitmap(mPhotoBitmap, 0, 0, null);
                    canvas.save();
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

        FileUtils.deleteBitmapFile(mContext);

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
            case IMPORT:
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
                savedMatrix.set(matrix);
                start.set(event.getX(), event.getY());
                mode = DRAG;
                lastEvent = null;
                break;
            case INK:
                mMatrix.setTranslate(x - mBitmap.getWidth() / 2f,
                        y - mBitmap.getHeight() / 2f - mBitmap.getWidth() * .15f);

                int inkX = Math.round(x), inkY = Math.round(y);
                if (eventCoordsInRange(inkX, inkY)) {
                    mInkedColor = mBitmap.getPixel(inkX, inkY);
                }
                break;
            case IMPORT:
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
                oldDist = spacing(event);
                if (oldDist > 10f) {
                    savedMatrix.set(matrix);
                    midPoint(mid, event);
                    mode = ZOOM;
                }
                lastEvent = new float[4];
                lastEvent[0] = event.getX(0);
                lastEvent[1] = event.getX(1);
                lastEvent[2] = event.getY(0);
                lastEvent[3] = event.getY(1);
                d = rotation(event);
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
                if (mode == DRAG) {
                    matrix.set(savedMatrix);
                    float dx = event.getX() - start.x;
                    float dy = event.getY() - start.y;
                    matrix.postTranslate(dx, dy);
                } else if (mode == ZOOM) {
                    float newDist = spacing(event);
                    if (newDist > 10f) {
                        matrix.set(savedMatrix);
                        float scale = (newDist / oldDist);
                        matrix.postScale(scale, scale, mid.x, mid.y);
                    }
                    if (lastEvent != null && event.getPointerCount() == 3) {
                        newRot = rotation(event);
                        float r = newRot - d;
                        float[] values = new float[9];
                        matrix.getValues(values);
                        float tx = values[2];
                        float ty = values[5];
                        float sx = values[0];
                        float xc = (mBitmap.getWidth() / 2) * sx;
                        float yc = (mBitmap.getHeight() / 2) * sx;
                        matrix.postRotate(r, tx + xc, ty + yc);
                    }
                }
                break;
            case INK:
                mMatrix.postTranslate(x - mLastX, y - mLastY);

                int inkX = Math.round(x), inkY = Math.round(y - mBitmap.getWidth() * .15f);
                if (eventCoordsInRange(inkX, inkY)) {
                    mInkedColor = mBitmap.getPixel(inkX, inkY);
                    mStrokeColor = mInkedColor;
                    setPaintColor(mStrokeColor);
                }
                break;
            case IMPORT:
                if (!mGestureDetector.isInProgress()) {
                    mMatrix.postTranslate(x - mLastX, y - mLastY);
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
            case IMPORT:
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

        switch (mState) {
            case TEXT:
                mode = NONE;
                lastEvent = null;
                break;
        }
    }

    private float spacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return FloatMath.sqrt(x * x + y * y);
    }

    /**
     * Calculate the mid point of the first two fingers
     */
    private void midPoint(PointF point, MotionEvent event) {
        float x = event.getX(0) + event.getX(1);
        float y = event.getY(0) + event.getY(1);
        point.set(x / 2, y / 2);
    }

    /**
     * Calculate the degree to be rotated by.
     *
     * @param event
     * @return Degrees
     */
    private float rotation(MotionEvent event) {
        double delta_x = (event.getX(0) - event.getX(1));
        double delta_y = (event.getY(0) - event.getY(1));
        double radians = Math.atan2(delta_y, delta_x);
        return (float) Math.toDegrees(radians);
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

            mAllHistory.push(mRedoneHistory.pop());

            mCanvas.drawBitmap(mCachedBitmap, 0, 0, null);

            mAllHistory.redraw(mCanvas);

            isSafeToDraw = true;
            return isSafeToDraw;
        }
        return false;
    }

    public boolean undo() {
        if (!mAllHistory.isEmpty()) {
            isSafeToDraw = false;

            mRedoneHistory.push(mAllHistory.pop());

            mCanvas.drawBitmap(mCachedBitmap, 0, 0, null);

            mAllHistory.redraw(mCanvas);

            isSafeToDraw = true;
            return isSafeToDraw;
        }
        return false;
    }

    public void ink() {
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

        mMatrix.setTranslate(0, height / 2f - mTextLayout.getHeight() / 2);

        listener.toggleOptionsMenuVisibility(true);
    }

    public void onEvent(EventColorChosen eventColorChosen) {
        int color = eventColorChosen.color;
        if (eventColorChosen.bool) {
            store.setLastBackgroundColor(color);

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
        mCurrentPoints.redrawPaint.set(mPaint);

        STROKE_WIDTH = paint.getStrokeWidth();

        setPaintThickness(STROKE_WIDTH);
    }

    public void onEvent(EventBitmapChosen eventBitmapChosen) {
        Intent data = eventBitmapChosen.data;
        InputStream inputStream = null;
        try {
            inputStream = mContext.getContentResolver().openInputStream(data.getData());
            mPhotoBitmap = BitmapFactory.decodeStream(inputStream, null, FileUtils.getBitmapOptions(mContext));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            if (inputStream != null) {
                try {
                    listener.toggleOptionsMenuVisibility(true);

                    changeState(State.IMPORT);

                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
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
        mPaint.setStrokeWidth(floater);
        mCurrentPoints.redrawPaint.setStrokeWidth(floater);
    }

    private boolean eventCoordsInRange(int x, int y) {
        return (0 <= x && x <= mBitmap.getWidth() - 1) &&
                (0 <= y && y <= mBitmap.getHeight() - 1);
    }

    private final class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {

        private static final float MAX_SCALE = 5.0f, MIN_SCALE = .1f;
        private float mScaleFactor = 1f;

        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {
            return true;
        }

        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            mScaleFactor *= detector.getScaleFactor();
            mScaleFactor = Math.max(MIN_SCALE, Math.min(mScaleFactor, MAX_SCALE));
            mMatrix.setScale(mScaleFactor, mScaleFactor,
                    detector.getFocusX(), detector.getFocusY());
            return true;
        }

        @Override
        public void onScaleEnd(ScaleGestureDetector detector) {
            super.onScaleEnd(detector);
        }
    }
}
