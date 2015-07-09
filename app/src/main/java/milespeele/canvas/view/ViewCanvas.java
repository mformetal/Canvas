package milespeele.canvas.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;

/**
 * Created by milespeele on 7/2/15.
 */
public class ViewCanvas extends View {

    private static float STROKE_WIDTH = 5f;
    private static float HALF_STROKE_WIDTH = STROKE_WIDTH / 2;

    private final static String BITMAP_KEY = "bitmap";

    private Paint mPaint;
    private Bitmap mBitmap;
    private Canvas mCanvas;
    private PaintPath mPath;
    private float lastTouchX, lastTouchY;
    private Matrix scaleMatrix;
    private final RectF dirtyRect = new RectF();
    private ArrayList<PaintPath> mPaths;
    private int currentColor;

    public ViewCanvas(Context context, AttributeSet attrs) {
        super(context, attrs);

        currentColor = Color.BLACK;

        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setColor(currentColor);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeWidth(STROKE_WIDTH);

        scaleMatrix = new Matrix();

        mPath = new PaintPath(mPaint);

        mPaths = new ArrayList<>();
        mPaths.add(mPath);
        setDrawingCacheEnabled(true);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        scaleMatrix.reset();
        scaleMatrix.setScale(w, h);

        mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mBitmap);
        for (PaintPath p : mPaths) {
            mCanvas.drawPath(p, p.getPaint());
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        for (PaintPath p: mPaths) {
            canvas.drawPath(p, p.getPaint());
        }
        canvas.drawBitmap(mBitmap, scaleMatrix, null);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float eventX = event.getX();
        float eventY = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                onTouchDown(eventX, eventY);
                return true;

            case MotionEvent.ACTION_MOVE:
            case MotionEvent.ACTION_UP:
                onTouchUp(event, eventX, eventY);
                break;

            default:
                return false;
        }

        invalidate(
                (int) (dirtyRect.left - HALF_STROKE_WIDTH),
                (int) (dirtyRect.top - HALF_STROKE_WIDTH),
                (int) (dirtyRect.right + HALF_STROKE_WIDTH),
                (int) (dirtyRect.bottom + HALF_STROKE_WIDTH));

        lastTouchX = eventX;
        lastTouchY = eventY;
        return true;
    }

    private void onTouchUp(MotionEvent event, float eventX, float eventY) {
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

    private void onTouchDown(float eventX, float eventY) {
        mPath.moveTo(eventX, eventY);
        lastTouchX = eventX;
        lastTouchY = eventY;
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
        dirtyRect.left = Math.min(lastTouchX, eventX);
        dirtyRect.right = Math.max(lastTouchX, eventX);
        dirtyRect.top = Math.min(lastTouchY, eventY);
        dirtyRect.bottom = Math.max(lastTouchY, eventY);
    }

    public void clearCanvas() {
        for (PaintPath p: mPaths) {
            p.reset();
        }
        mPaths.clear();
        invalidate();
        mPath = new PaintPath(generatePaintWithColor(currentColor));
        mPaths.add(mPath);
    }

    public void changeColor(int color) {
        currentColor = color;
        mPath = new PaintPath(generatePaintWithColor(currentColor));
        mPaths.add(mPath);
    }

    private PaintPath getLatestPath() {
        if (mPaths.size() > 1) {
            return mPaths.get(mPaths.size() - 1);
        } else if (mPaths.size() == 1) {
            return mPaths.get(0);
        } else {
            return null;
        }
    }

    public Bitmap getBitmap() {
        return getDrawingCache();
    }

    public void undo() {
        PaintPath path = getLatestPath();
        if (path != null) {
            path.reset();
            mPaths.remove(path);
            invalidate();
        } else {
            mPath = new PaintPath(generatePaintWithColor(currentColor));
            mPaths.add(mPath);
        }
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Bundle state = new Bundle();
        state.putParcelable("super", super.onSaveInstanceState());
        state.putParcelable(BITMAP_KEY, getDrawingCache());
        return state;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        Bundle bundle = (Bundle) state;
        super.onRestoreInstanceState(bundle.getParcelable("super"));
        mBitmap = bundle.getParcelable(BITMAP_KEY);
        mCanvas = new Canvas(mBitmap);
        mCanvas.drawBitmap(mBitmap, 0, 0, mPaint);
    }

    private Paint generatePaintWithColor(int color) {
        Paint mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setColor(color);
        mPaint.setStrokeWidth(STROKE_WIDTH);
        return mPaint;
    }

    private class PaintPath extends Path {

        private Paint paint;

        public PaintPath(Paint paint) {
            this.paint = paint;
        }

        public Paint getPaint() {
            return paint;
        }
    }
}