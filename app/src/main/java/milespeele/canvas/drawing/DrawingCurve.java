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
import android.os.Handler;
import android.os.SystemClock;
import android.support.design.widget.Snackbar;
import android.text.DynamicLayout;
import android.text.Layout;
import android.text.TextPaint;
import android.view.MotionEvent;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import javax.inject.Inject;

import de.greenrobot.event.EventBus;
import milespeele.canvas.MainApp;
import milespeele.canvas.R;
import milespeele.canvas.event.EventBitmapChosen;
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
        INK,
        IMPORT
    }

    private DynamicLayout mTextLayout;
    private Matrix mMatrix, mSavedMatrix;
    private Bitmap mBitmap, mCachedBitmap, mPhotoBitmap;
    private Canvas mCanvas;
    private DrawingPoints mCurrentPoints;
    private DrawingHistory mRedoneHistory, mAllHistory;
    private Paint mPaint, mInkPaint;
    private TextPaint mTextPaint;
    private State mState = State.DRAW;
    private Context mContext;
    private PointF mStartPoint, mMidPoint;

    private static final float TOLERANCE = 5f;
    private static float STROKE_WIDTH = 5f;
    private static final int INVALID_POINTER = -1;
    private static final int NONE = 0, DRAG = 1, ZOOM = 2;
    private int mMode = NONE;
    private int mActivePointer = INVALID_POINTER;
    private float mLastX, mLastY;
    private int mStrokeColor, mBackgroundColor, mOppositeBackgroundColor, mInkedColor;
    private boolean isSafeToDraw = true;
    private double mOldDist = 1f;
    private float mLastRotation = 0f;
    private float[] mLastEvent = null;

    @Inject Datastore store;
    @Inject EventBus bus;

    private DrawingCurveListener listener;
    public interface DrawingCurveListener {
        void onDrawingCurveOptionsMenuVisibilityRequest(boolean visible, State state);
        void onDrawingCurveFabMenuVisibilityRequest(boolean visible);
        void onDrawingCurveSnbackRequest(int stringId, int length);
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
        mSavedMatrix = new Matrix();
        mStartPoint = new PointF();
        mMidPoint = new PointF();

        mCachedBitmap = FileUtils.getCachedBitmap(mContext);
        if (mCachedBitmap == null) {
            mCachedBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
            mCachedBitmap.eraseColor(mBackgroundColor);
        }

        mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mBitmap);
        mCanvas.drawBitmap(mCachedBitmap, 0, 0, null);

        mPaint = PaintStyles.normal(mStrokeColor, 5f);
        mInkPaint = PaintStyles.normal(mStrokeColor, 20f);

        mTextPaint = new TextPaint(TextPaint.ANTI_ALIAS_FLAG);
        mTextPaint.setColor(mStrokeColor);
        mTextPaint.setStyle(Paint.Style.FILL_AND_STROKE);

        mAllHistory = new DrawingHistory();
        mRedoneHistory = new DrawingHistory();
        mCurrentPoints = new DrawingPoints(mPaint);
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

    public void onOptionsMenuCancel() {
        listener.onDrawingCurveOptionsMenuVisibilityRequest(false, null);
        listener.onDrawingCurveFabMenuVisibilityRequest(true);

        changeState(State.DRAW);
        ViewUtils.setIdentityMatrix(mMatrix);

        switch (mState) {
            case TEXT:
                mTextLayout = null;
                break;
            case IMPORT:
                mPhotoBitmap.recycle();
                mPhotoBitmap = null;
                break;
        }
    }

    public void onOptionsMenuAccept() {
        switch (mState) {
            case TEXT:
                listener.onDrawingCurveOptionsMenuVisibilityRequest(false, null);
                listener.onDrawingCurveFabMenuVisibilityRequest(true);

                mCanvas.save();
                mCanvas.concat(mMatrix);
                mTextLayout.draw(mCanvas);
                mCanvas.restore();

//                mAllHistory.push(new DrawingText(mTextLayout.getText(), mLastX, mLastY, scaleFactor, mTextPaint));

                changeState(State.DRAW);

                ViewUtils.setIdentityMatrix(mMatrix);

                mTextLayout = null;
                break;
            case IMPORT:
                listener.onDrawingCurveOptionsMenuVisibilityRequest(false, null);
                listener.onDrawingCurveFabMenuVisibilityRequest(true);

                mCanvas.save();
                mCanvas.concat(mMatrix);
                mCanvas.drawBitmap(mPhotoBitmap, 0, 0, null);
                mCanvas.restore();

                changeState(State.DRAW);

                ViewUtils.setIdentityMatrix(mMatrix);

                mPhotoBitmap.recycle();
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
                    int count = canvas.save();
                    canvas.concat(mMatrix);
                    mTextLayout.draw(canvas);
                    canvas.restoreToCount(count);
                    break;
                case INK:
                    float lineSize = canvas.getWidth() * .1f, xSpace = canvas.getWidth() * .05f;
                    float middleX = mLastX, middleY = mLastY - canvas.getHeight() * .1f;

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
                    break;
                case IMPORT:
                    int saveCount = canvas.save();
                    canvas.concat(mMatrix);
                    canvas.drawBitmap(mPhotoBitmap, 0, 0, null);
                    canvas.restoreToCount(saveCount);
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

    public boolean onTouchEvent(MotionEvent event) {
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

    private void onTouchDown(MotionEvent event) {
        float x = event.getX(), y = event.getY();

        switch (mState) {
            case TEXT:
            case IMPORT:
                mSavedMatrix.set(mMatrix);
                mMode = DRAG;
                mStartPoint.set(x, y);
                mLastEvent = null;
                break;
        }

        mActivePointer = event.getPointerId(0);
        mLastX = x;
        mLastY = y;
    }

    public void onPointerDown(MotionEvent event) {
        switch (mState) {
            case TEXT:
            case IMPORT:
                if (event.getPointerCount() <= 2) {
                    mOldDist = calculdateDistance(event);
                    if (mOldDist > 10f) {
                        mSavedMatrix.set(mMatrix);
                        calculateMidpoint(mMidPoint, event);
                        mMode = ZOOM;
                    }
                    mLastEvent = new float[4];
                    mLastEvent[0] = event.getX(0);
                    mLastEvent[1] = event.getX(1);
                    mLastEvent[2] = event.getY(0);
                    mLastEvent[3] = event.getY(1);
                    mLastRotation = calculateTouchAngle(event);
                }
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
            case INK:
                int inkx = Math.round(x), inky = Math.round(y - mBitmap.getHeight() * .095f);
                if (eventCoordsInRange(inkx, inky)) {
                    mInkedColor = mBitmap.getPixel(inkx, inky);
                }
                break;
            case TEXT:
            case IMPORT:
                if (mMode == DRAG) {
                    mMatrix.set(mSavedMatrix);
                    mMatrix.postTranslate(x - mStartPoint.x, y - mStartPoint.y);
                } else if (mMode == ZOOM) {
                    if (event.getPointerCount() == 2) {
                        double newDist = calculdateDistance(event);
                        if (newDist > 10f) {
                            mMatrix.set(mSavedMatrix);
                            double scale = (newDist / mOldDist);
                            mMatrix.postScale((float) scale, (float) scale, mMidPoint.x, mMidPoint.y);
                        }

                        float mCurrentRotation = calculateTouchAngle(event);
                        if (Math.abs(mCurrentRotation - mLastRotation) >= 10f) {
                            mMatrix.postRotate(mCurrentRotation - mLastRotation,
                                    mMidPoint.x, mMidPoint.y);
                        }
                    }
                }
                break;
        }

        mLastX = x;
        mLastY = y;
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
            case IMPORT:
                mMode = NONE;
                mLastEvent = null;
                break;
        }
    }

    private void onTouchUp(MotionEvent event) {
        mActivePointer = INVALID_POINTER;

        switch (mState) {
            case ERASE:
            case DRAW:
                mCurrentPoints.storePoints();
                mAllHistory.push(mCurrentPoints);
                mCurrentPoints.clear();
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
        mActivePointer = INVALID_POINTER;
    }

    private double calculdateDistance(MotionEvent event) {
        double dx = event.getX(0) - event.getX(1);
        double dy = event.getY(0) - event.getY(1);
        return Math.sqrt(dx * dx + dy * dy);
    }

    private void calculateMidpoint(PointF point, MotionEvent event) {
        float x = event.getX(0) + event.getX(1);
        float y = event.getY(0) + event.getY(1);
        point.set(x / 2, y / 2);
    }

    private float calculateTouchAngle(MotionEvent event) {
        double dx = (event.getX(0) - event.getX(1));
        double dy = (event.getY(0) - event.getY(1));
        double radians = Math.atan2(dy, dx);
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

        listener.onDrawingCurveFabMenuVisibilityRequest(false);

        int middleX = mBitmap.getWidth() / 2;
        int middleY = mBitmap.getHeight() / 2;

        mLastX = middleX;
        mLastY = middleY;

        mInkedColor = mBitmap.getPixel(middleX, middleY);
        mInkPaint.setColor(mInkedColor);
    }

    public void erase() {
        if (mState == State.ERASE) {
            changeState(State.DRAW);
        } else {
            changeState(State.ERASE);
        }
    }

    public void onEvent(EventTextChosen eventTextChosen) {
        switch (mState) {
            case DRAW:
                int width = mBitmap.getWidth(), height = mBitmap.getHeight();

                String textToBeDrawn = eventTextChosen.text;

                TextUtils.adjustTextSize(mTextPaint, textToBeDrawn, height);
                TextUtils.adjustTextScale(mTextPaint, textToBeDrawn, width, 0, 0);

                mTextLayout = new DynamicLayout(textToBeDrawn, mTextPaint, mBitmap.getWidth(),
                        Layout.Alignment.ALIGN_CENTER, 0, 0, false);

                changeState(State.TEXT);

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        listener.onDrawingCurveOptionsMenuVisibilityRequest(true, State.TEXT);
                        listener.onDrawingCurveFabMenuVisibilityRequest(false);
                    }
                }, 350);
                break;
            case TEXT:
//                mTextLayout.getText().
                break;
        }
    }

    public void onEvent(EventColorChosen eventColorChosen) {
        int color = eventColorChosen.color;

        switch (mState) {
            case DRAW:
                if (eventColorChosen.bool) {
                    store.setLastBackgroundColor(color);

                    reset(color);

                    changeState(State.DRAW);
                } else {
                    changeState(State.DRAW);

                    setPaintColor(color);
                }
                break;
            case TEXT:
                mTextPaint.setColor(color);
                mTextLayout.getPaint().setColor(color);
                break;
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
                    listener.onDrawingCurveOptionsMenuVisibilityRequest(true, State.IMPORT);
                    listener.onDrawingCurveFabMenuVisibilityRequest(false);

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
}
