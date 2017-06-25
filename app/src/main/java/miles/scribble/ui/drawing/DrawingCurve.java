package miles.scribble.ui.drawing;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.view.MotionEvent;

import org.greenrobot.eventbus.EventBus;

import java.io.IOException;
import java.io.InputStream;
import java.util.Stack;

import javax.inject.Inject;

import io.realm.Realm;
import miles.scribble.MainApp;
import miles.scribble.R;
import miles.scribble.data.model.Sketch;
import miles.scribble.data.event.EventBitmapChosen;
import miles.scribble.data.event.EventBrushChosen;
import miles.scribble.data.event.EventClearCanvas;
import miles.scribble.data.event.EventColorChosen;
import miles.scribble.data.event.EventTextChosen;
import miles.scribble.data.Datastore;
import miles.scribble.data.event.EventUpdateDrawingCurve;
import miles.scribble.util.FileUtils;
import miles.scribble.util.Logg;
import miles.scribble.util.PaintStyles;
import miles.scribble.util.ViewUtils;
import rx.Observable;
import rx.Subscriber;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by mbpeele on 9/25/15.
 */
public class DrawingCurve {

    public enum State {
        DRAW,
        ERASE,
        TEXT,
        INK,
        PICTURE
    }

    private final Matrix mMatrix, mSavedMatrix;
    private Bitmap mBitmap, mCachedBitmap, mPhotoBitmap;
    private Canvas mCanvas;
    private Stroke mStroke;
    private Stack<Object> mRedoneHistory;
    private final Stack<Object> mAllHistory;
    private Paint mPaint, mInkPaint;
    private TextPaint mTextPaint;
    private StaticLayout mTextLayout;
    private State mState = State.DRAW;
    private Context mContext;
    private PointF mStartPoint, mMidPoint;
    private Uri mPhotoBitmapUri;
    private Handler mHandler;

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
    private boolean shouldUpdate;

    @Inject Datastore store;
    @Inject
    EventBus bus;
    private BitmapCache cache;

    private DrawingCurveListener mListener;
    public interface DrawingCurveListener {
        void toggleOptionsMenuVisibilty(boolean visible, State state);
        void toggleFabMenuVisibility(boolean visible);
    }

    public DrawingCurve(Context context) {
        ((MainApp) context.getApplicationContext()).getApplicationComponent().inject(this);

        mContext = context;

        cache = new BitmapCache(mContext);

        Point size = new Point();
        ((Activity) context).getWindowManager().getDefaultDisplay().getRealSize(size);
        int w = size.x;
        int h = size.y;

        mStrokeColor = ViewUtils.randomColor();
        mBackgroundColor = store.getLastBackgroundColor();
        mOppositeBackgroundColor = ViewUtils.complementColor(mBackgroundColor);
        mInkedColor = mStrokeColor;

        mMatrix = new Matrix();
        mSavedMatrix = new Matrix();
        mStartPoint = new PointF();
        mMidPoint = new PointF();

        mCachedBitmap = FileUtils.getCachedBitmap(mContext);
        if (mCachedBitmap == null) {
            mCachedBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
            mCachedBitmap.eraseColor(Color.WHITE);
        }

        mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mBitmap);
        mCanvas.drawBitmap(mCachedBitmap, 0, 0, null);

        mPaint = PaintStyles.normal(mStrokeColor, 5f);
        mInkPaint = PaintStyles.normal(mStrokeColor, 20f);

        mTextPaint = new TextPaint(TextPaint.ANTI_ALIAS_FLAG);
        mTextPaint.setColor(mStrokeColor);
        mTextPaint.setTextSize(mContext.getResources().getDimension(R.dimen.large_text_size) * 4f);
        mTextPaint.setStyle(Paint.Style.FILL_AND_STROKE);

        mAllHistory = new Stack<>();
        mRedoneHistory = new Stack<>();
        mStroke = new Stroke(mPaint);

        mHandler = new Handler(Looper.getMainLooper());
    }

    public void setListener(DrawingCurveListener listener) {
        mListener = listener;
    }

    public void drawToSurfaceView(Canvas canvas) {
        if (canvas != null && isSafeToDraw) {
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
                case PICTURE:
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
            case ERASE:
            case DRAW:
                mStroke.addPoint(x, y, mCanvas, mPaint);
                break;
            case TEXT:
            case PICTURE:
                mSavedMatrix.set(mMatrix);
                mMode = DRAG;
                mStartPoint.set(x, y);
                break;
        }

        mActivePointer = event.getPointerId(0);
        mLastX = x;
        mLastY = y;
    }

    private void onPointerDown(MotionEvent event) {
        switch (mState) {
            case TEXT:
            case PICTURE:
                if (event.getPointerCount() <= 2) {
                    mOldDist = distance(event);
                    if (mOldDist > 10f) {
                        mSavedMatrix.set(mMatrix);
                        midpoint(mMidPoint, event);
                        mMode = ZOOM;
                    }
                    mLastRotation = angle(event);
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
                    mStroke.addPoint(event.getHistoricalX(pointerIndex, i),
                            event.getHistoricalY(pointerIndex, i), mCanvas, mPaint);
                }
                mStroke.addPoint(x, y, mCanvas, mPaint);
                break;
            case INK:
                int inkx = Math.round(x), inky = Math.round(y - mBitmap.getHeight() * .095f);
                if ((0 <= inkx && inkx <= mBitmap.getWidth() - 1) &&
                    (0 <= inky && inky <= mBitmap.getHeight() - 1)) {
                    mInkedColor = mBitmap.getPixel(inkx, inky);
                }
                break;
            case TEXT:
            case PICTURE:
                if (mMode == DRAG) {
                    mMatrix.set(mSavedMatrix);
                    mMatrix.postTranslate(x - mStartPoint.x, y - mStartPoint.y);
                } else if (mMode == ZOOM) {
                    if (event.getPointerCount() == 2) {
                        double newDist = distance(event);
                        if (newDist > 10f) {
                            mMatrix.set(mSavedMatrix);
                            double scale = (newDist / mOldDist);
                            mMatrix.postScale((float) scale, (float) scale, mMidPoint.x, mMidPoint.y);
                        }

                        float mCurrentRotation = angle(event);
                        mMatrix.postRotate(mCurrentRotation - mLastRotation,
                                mMidPoint.x, mMidPoint.y);
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
            case PICTURE:
                mMode = NONE;
                break;
        }
    }

    private void onTouchUp(MotionEvent event) {
        mActivePointer = INVALID_POINTER;

        switch (mState) {
            case ERASE:
            case DRAW:
                mAllHistory.push(new NormalDrawHistory(mStroke, mStroke.paint));
                mStroke.clear();
                break;
            case INK:
                mStrokeColor = mInkedColor;
                setPaintColor(mStrokeColor);
                changeState(State.DRAW);

                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mListener.toggleFabMenuVisibility(true);
                    }
                }, 350);
                break;
        }

        mLastX = event.getX();
        mLastY = event.getY();
    }

    private void onCancel(MotionEvent event) {
        mActivePointer = INVALID_POINTER;
    }

    private double distance(MotionEvent event) {
        double dx = event.getX(0) - event.getX(1);
        double dy = event.getY(0) - event.getY(1);
        return Math.sqrt(dx * dx + dy * dy);
    }

    private void midpoint(PointF point, MotionEvent event) {
        float x = event.getX(0) + event.getX(1);
        float y = event.getY(0) + event.getY(1);
        point.set(x / 2, y / 2);
    }

    private float angle(MotionEvent event) {
        double dx = (event.getX(0) - event.getX(1));
        double dy = (event.getY(0) - event.getY(1));
        double radians = Math.atan2(dy, dx);
        return (float) Math.toDegrees(radians);
    }

    public boolean redo() {
        if (!mRedoneHistory.isEmpty()) {
            mAllHistory.push(mRedoneHistory.pop());

            redraw();

            return true;
        }
        return false;
    }

    public boolean undo() {
        if (!mAllHistory.isEmpty()) {
            mRedoneHistory.push(mAllHistory.pop());

            redraw();

            return true;
        }
        return false;
    }

    private void redraw() {
        final Bitmap worker = Bitmap.createBitmap(mBitmap);
        final Canvas workerCanvas = new Canvas(worker);
        workerCanvas.drawColor(mBackgroundColor, PorterDuff.Mode.CLEAR);

        synchronized (mAllHistory) {
            Observable.from(mAllHistory)
                    .doOnError(new Action1<Throwable>() {
                        @Override
                        public void call(Throwable throwable) {
                            Logg.log(throwable);
                        }
                    })
                    .subscribeOn(Schedulers.io())
                    .subscribe(new Subscriber<Object>() {
                        @Override
                        public void onCompleted() {
                            mBitmap = worker.copy(Bitmap.Config.ARGB_8888, true);
                            mCanvas = new Canvas(mBitmap);

                            worker.recycle();

                            isSafeToDraw = true;
                        }

                        @Override
                        public void onError(Throwable e) {

                        }

                        @Override
                        public void onNext(Object object) {
                            if (object instanceof NormalDrawHistory) {
                                ((NormalDrawHistory) object).draw(workerCanvas);
                            } else if (object instanceof BitmapDrawHistory) {
                                ((BitmapDrawHistory) object).draw(mMatrix, cache, mContext, workerCanvas);
                            } else if (object instanceof TextDrawHistory) {
                                ((TextDrawHistory) object).draw(workerCanvas, mMatrix);
                            }
                        }

                        @Override
                        public void onStart() {
                            super.onStart();

                            isSafeToDraw = false;

                            workerCanvas.drawBitmap(mCachedBitmap, 0, 0, null);
                        }
                    });
        }
    }

    public void ink() {
        changeState(State.INK);

        mListener.toggleFabMenuVisibility(false);

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

    public void onOptionsMenuCancel() {
        mListener.toggleOptionsMenuVisibilty(false, null);
        mListener.toggleFabMenuVisibility(true);

        changeState(State.DRAW);

        ViewUtils.identityMatrix(mMatrix);

        switch (mState) {
            case TEXT:
                mTextLayout = null;
                break;
            case PICTURE:
                mPhotoBitmap.recycle();
                mPhotoBitmap = null;
                break;
        }
    }

    public void onOptionsMenuAccept() {
        float[] values = new float[9];
        switch (mState) {
            case TEXT:
                mListener.toggleOptionsMenuVisibilty(false, null);
                mListener.toggleFabMenuVisibility(true);

                mCanvas.save();
                mCanvas.concat(mMatrix);
                mTextLayout.draw(mCanvas);
                mCanvas.restore();

                changeState(State.DRAW);

                mMatrix.getValues(values);
                mAllHistory.push(new TextDrawHistory(mTextLayout.getText(), values, mTextPaint));

                ViewUtils.identityMatrix(mMatrix);

                mTextLayout = null;
                break;
            case PICTURE:
                mListener.toggleOptionsMenuVisibilty(false, null);
                mListener.toggleFabMenuVisibility(true);

                mCanvas.save();
                mCanvas.concat(mMatrix);
                mCanvas.drawBitmap(mPhotoBitmap, 0, 0, null);
                mCanvas.restore();

                changeState(State.DRAW);

                cache.put(mPhotoBitmapUri, mPhotoBitmap.copy(mPhotoBitmap.getConfig(), true));

                mMatrix.getValues(values);
                mAllHistory.push(new BitmapDrawHistory(mPhotoBitmapUri, values));

                ViewUtils.identityMatrix(mMatrix);

                mPhotoBitmapUri = null;
                mPhotoBitmap.recycle();
                mPhotoBitmap = null;
                break;
        }
    }

    @SuppressWarnings("unused")
    public void onEvent(EventUpdateDrawingCurve eventUpdateDrawingCurve) {
        isSafeToDraw = false;

        Realm realm = Realm.getDefaultInstance();
        Sketch sketch = realm.where(Sketch.class).equalTo("id", eventUpdateDrawingCurve.id).findFirst();
        realm.close();

        mAllHistory.clear();
        mRedoneHistory.clear();
        mStroke.clear();

        mCachedBitmap = BitmapFactory.decodeByteArray(sketch.getBytes(), 0,
                sketch.getBytes().length, FileUtils.getBitmapOptions());
        mBitmap = Bitmap.createBitmap(mCachedBitmap.getWidth(), mCachedBitmap.getHeight(), Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mBitmap);
        mCanvas.drawBitmap(mCachedBitmap, 0, 0, null);

        isSafeToDraw = true;
    }

    @SuppressWarnings("unused")
    public void onEvent(EventClearCanvas eventClearCanvas) {
        isSafeToDraw = false;

        int width = mBitmap.getWidth(), height = mBitmap.getHeight();

        FileUtils.deleteBitmapFile(mContext, FileUtils.DRAWING_BITMAP_FILENAME);

        mStroke.clear();
        mAllHistory.clear();
        mRedoneHistory.clear();

        mCachedBitmap.recycle();
        mCachedBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        mBitmap.recycle();
        mBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mBitmap);

        isSafeToDraw = true;
    }

    @SuppressWarnings("unused")
    public void onEvent(EventTextChosen eventTextChosen) {
        mTextLayout = new StaticLayout(eventTextChosen.text, mTextPaint, mBitmap.getWidth(),
                Layout.Alignment.ALIGN_CENTER, 1, 1, false);

        switch (mState) {
            case DRAW:
                mMatrix.postTranslate(0, mBitmap.getHeight() / 2f);

                changeState(State.TEXT);

                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mListener.toggleOptionsMenuVisibilty(true, State.TEXT);
                        mListener.toggleFabMenuVisibility(false);
                    }
                }, 350);
                break;
        }
    }

    @SuppressWarnings("unused")
    public void onEvent(EventColorChosen eventColorChosen) {
        int color = eventColorChosen.color;

        switch (mState) {
            case DRAW:
                if (eventColorChosen.fill) {
                    mBackgroundColor = color;

                    store.setLastBackgroundColor(mBackgroundColor);

                    mStroke.clear();
                    mAllHistory.clear();
                    mRedoneHistory.clear();


                    mCachedBitmap.eraseColor(mBackgroundColor);
                    mBitmap.eraseColor(mBackgroundColor);

                    mOppositeBackgroundColor = ViewUtils.complementColor(mBackgroundColor);
                } else {
                    mStrokeColor = color;

                    setPaintColor(color);
                }
                break;
            case TEXT:
                mTextPaint.setColor(color);
                break;
            case ERASE:
                mStrokeColor = color;
                changeState(State.DRAW);
                break;
        }
    }

    @SuppressWarnings("unused")
    public void onEvent(EventBrushChosen eventBrushChosen) {
        Paint paint = eventBrushChosen.paint;

        changeState(State.DRAW);

        mPaint.set(paint);
        mStroke.paint.set(mPaint);

        STROKE_WIDTH = paint.getStrokeWidth();
    }

    @SuppressWarnings("unused")
    public void onEvent(EventBitmapChosen eventBitmapChosen) {
        if (mPhotoBitmap != null) {
            mPhotoBitmap.recycle();
        }

        ViewUtils.identityMatrix(mMatrix);

        mPhotoBitmapUri = eventBitmapChosen.data;
        InputStream inputStream = null;
        if (mPhotoBitmapUri != null) {
            try {
                inputStream = mContext.getContentResolver().openInputStream(mPhotoBitmapUri);
                mPhotoBitmap = FileUtils.getBitmapFromStream(inputStream);

                float scale = Math.min((float) mBitmap.getWidth() / mPhotoBitmap.getWidth(),
                        (float) mBitmap.getHeight() / mPhotoBitmap.getHeight());
                scale = Math.max(scale, Math.min((float) mBitmap.getHeight() / mPhotoBitmap.getWidth(),
                        (float) mBitmap.getWidth() / mPhotoBitmap.getHeight()));
                if (scale < 1) {
                    mMatrix.setScale(scale, scale);
                }
            } catch (IOException e) {
                Logg.log(e);
            } finally {
                if (inputStream != null) {
                    try {
                        mHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                mListener.toggleOptionsMenuVisibilty(true, State.PICTURE);
                                mListener.toggleFabMenuVisibility(false);
                            }
                        }, 350);

                        changeState(State.PICTURE);

                        inputStream.close();
                    } catch (IOException e) {
                        Logg.log(e);
                    }
                }
            }
        }
    }

    public int getStrokeColor() { return mPaint.getColor(); }

    public int getBackgroundColor() { return mBackgroundColor; }

    public Bitmap getBitmap() { return mBitmap; }

    public Paint getPaint() { return mPaint; }

    public State getState() { return mState; }

    private void setPaintColor(int color) {
        mTextPaint.setColor(color);
        mPaint.setColor(color);
        mStroke.paint.setColor(color);
    }

    private void setPaintThickness(float floater) {
        mPaint.setStrokeWidth(floater);
        mStroke.paint.setStrokeWidth(floater);
    }
}
