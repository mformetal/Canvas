package milespeele.canvas.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.LruCache;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;

import milespeele.canvas.util.Logger;

/**
 * Created by milespeele on 7/2/15.
 */
public class ViewCanvas extends View {

    private Bitmap mBitmap;
    private Canvas mCanvas;
    private float lastX, lastY;
    private Matrix scaleMatrix;
    private LruCache<String, PaintPath> mMemoryCache;
    private PaintPath mPath;
    private int currentColor;
    private Paint mPaint;
    private static final float TOLERANCE = 5;
    private static final float STROKE_WIDTH = 4f;
    private static final float HALF_STROKE_WIDTH = STROKE_WIDTH / 2;
    private final RectF dirtyRect = new RectF();

    public ViewCanvas(Context c, AttributeSet attrs) {
        super(c, attrs);

        scaleMatrix = new Matrix();

        currentColor = Color.BLACK;
        initializePaint(currentColor);
        mPath = new PaintPath();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        scaleMatrix.reset();
        scaleMatrix.setScale(w, h);

        mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mBitmap);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        mCanvas.drawPath(mPath, mPaint);
    }

    private void initializePaint(int color) {
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setColor(color);
        mPaint.setStrokeWidth(STROKE_WIDTH);
    }

    private void startTouch(float x, float y) {
        mPath.moveTo(x, y);
        lastX = x;
        lastY = y;
    }

    private void upTouch(float eventX, float eventY, MotionEvent event) {
        resetDirtyRect(eventX, eventY);

        int historySize = event.getHistorySize();
        for (int i = 0; i < historySize; i++) {
            float historicalX = event.getHistoricalX(i);
            float historicalY = event.getHistoricalY(i);
            expandDirtyRect(historicalX, historicalY);
            mPath.lineTo(historicalX, historicalY);
        }

        mPath.lineTo(eventX, eventY);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                startTouch(x, y);
                return true;
            case MotionEvent.ACTION_MOVE:

            case MotionEvent.ACTION_UP:
                upTouch(x, y, event);
                break;

            default:
                Logger.log("Ignored touch event: " + event.toString());
                return false;
        }

        invalidate(
                (int) (dirtyRect.left - HALF_STROKE_WIDTH),
                (int) (dirtyRect.top - HALF_STROKE_WIDTH),
                (int) (dirtyRect.right + HALF_STROKE_WIDTH),
                (int) (dirtyRect.bottom + HALF_STROKE_WIDTH));

        lastX = x;
        lastY = y;
        return true;
    }

    private void expandDirtyRect(float historicalX, float historicalY) {
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

    private void resetDirtyRect(float eventX, float eventY) {
        dirtyRect.left = Math.min(lastX, eventX);
        dirtyRect.right = Math.max(lastX, eventX);
        dirtyRect.top = Math.min(lastY, eventY);
        dirtyRect.bottom = Math.max(lastY, eventY);
    }

    public void changeToEraser() {
        // TO DO
    }

    public void clearCanvas() {
        mPath.reset();
        invalidate();
    }

    public void changeColor(int color) {
        currentColor = color;
        mPaint.setColor(currentColor);
    }

    public void undo() {
//        PaintPath latestPath = getLatestPath();
//        latestPath.reset();
//        mPaths.remove(latestPath);
//        invalidate();
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);

        // Use 1/8th of the available memory for this memory cache.
        final int cacheSize = maxMemory / 8;

        mMemoryCache = new LruCache<String, PaintPath>(cacheSize) {
            @Override
            protected int sizeOf(String key, PaintPath path) {
                // The cache size will be measured in kilobytes rather than
                // number of items.
                return 5000;
            }
        };
        mMemoryCache.put("test", mPath);
        return super.onSaveInstanceState();
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        mPath = mMemoryCache.get("test");
        mCanvas.drawPath(mPath, mPaint);
        super.onRestoreInstanceState(state);
    }

    private class PaintPath extends Path {

        private ArrayList<PointPair> points;

        public PaintPath() {
            points = new ArrayList<>();
        }
    }

    private class PointPair {
        private float x;
        private float y;

        public PointPair(float x, float y) {
            this.x = x;
            this.y = y;
        }
    }
}