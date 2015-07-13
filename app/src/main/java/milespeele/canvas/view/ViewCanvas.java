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
import android.util.LruCache;
import android.view.MotionEvent;
import android.view.View;

import java.util.EmptyStackException;
import java.util.Random;
import java.util.Stack;

import milespeele.canvas.util.Logger;

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
    private float lastTouchX, lastTouchY;
    private Matrix scaleMatrix;
    private final RectF dirtyRect = new RectF();
    private PaintPath mPath;
    private Stack<PaintPath> mPaths;
    private Stack<PaintPath> redoPaths;

    private int width, height;

    public ViewCanvas(Context context) {
        super(context);
        init();
    }

    public ViewCanvas(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public void init() {
        Random rnd = new Random();
        int color = Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));

        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setColor(color);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeWidth(STROKE_WIDTH);
        mPaint.setStrokeCap(Paint.Cap.ROUND);

        scaleMatrix = new Matrix();

        mPath = new PaintPath(mPaint.getColor());
        mPaths = new Stack<>();
        redoPaths = new Stack<>();

        mPaths.push(mPath);

        setDrawingCacheEnabled(true);
        setSaveEnabled(true);
        setBackgroundColor(Color.WHITE);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        width = w;
        height = h;

        scaleMatrix.reset();
        scaleMatrix.setScale(w, h);

        mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mBitmap);
        for (PaintPath p : mPaths) {
            mPaint.setColor(p.getColor());
            mCanvas.drawPath(p, mPaint);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        for (PaintPath p: mPaths) {
            mPaint.setColor(p.getColor());
            canvas.drawPath(p, mPaint);
        }
        canvas.drawBitmap(mBitmap, scaleMatrix, null);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float eventX = event.getX();
        float eventY = event.getY();
        float time = event.getDownTime();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                onTouchDown(eventX, eventY, time);
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

    private void onTouchDown(float eventX, float eventY, float time) {
        mPath = new PaintPath(mPaint.getColor());
        mPaths.push(mPath);
        mPath.moveTo(eventX, eventY);
        lastTouchX = eventX;
        lastTouchY = eventY;
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

    public void fillCanvas(int color) {
        setBackgroundColor(color);
        setDrawingCacheBackgroundColor(color);
    }

    public void clearCanvas() {
        for (PaintPath p: mPaths) {
            p.reset();
        }
        mPaths.clear();

        destroyDrawingCache();

        mPath = new PaintPath(mPaint.getColor());
        mPaths.push(mPath);

        invalidate();

        mBitmap.recycle();
        mBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mBitmap);
        buildDrawingCache(true);
    }

    public void changeColor(int color) {
        mPaint.setColor(color);
        mPath = new PaintPath(mPaint.getColor());
        mPaths.push(mPath);
    }

    private PaintPath getLatestPath() {
        try {
            return mPaths.peek();
        } catch (EmptyStackException e) {
            return null;
        }
    }

    public Bitmap getBitmap() {
        return Bitmap.createBitmap(getDrawingCache(true));
    }

    public void undo() {
        PaintPath path = getLatestPath();
        if (path != null) {
            PaintPath redo = new PaintPath(mPaint.getColor());
            redo.set(path);
            redoPaths.push(redo);
            path.rewind();
            mPaths.pop();
            invalidate();
        }
    }

    public void redo() {
        if (!redoPaths.isEmpty()) {
            mPaths.push(redoPaths.pop());
            invalidate();
        }
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Logger.log("ONSAVE");
        Bundle state = new Bundle();
        state.putParcelable("super", super.onSaveInstanceState());
        state.putParcelable(BITMAP_KEY, Bitmap.createBitmap(getDrawingCache(true)));
        return state;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        Logger.log("ON RESUME");
        Bundle bundle = (Bundle) state;
        super.onRestoreInstanceState(bundle.getParcelable("super"));
        mBitmap = bundle.getParcelable(BITMAP_KEY);
        mCanvas = new Canvas(mBitmap);
        mCanvas.drawBitmap(mBitmap, scaleMatrix, mPaint);
    }
}