package milespeele.canvas.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.EmptyStackException;
import java.util.Random;
import java.util.Stack;

/**
 * Created by milespeele on 7/2/15.
 */
public class ViewCanvas extends View {

    private static float STROKE_WIDTH = 5f;
    private static float HALF_STROKE_WIDTH = STROKE_WIDTH / 2;

    private int currentColor;
    private Paint curPaint;
    private Bitmap mBitmap;
    private Canvas mCanvas;
    private float lastTouchX, lastTouchY;
    private Matrix scaleMatrix;
    private final RectF dirtyRect = new RectF();
    private PaintPath mPath;
    private PaintStack mPaths;
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
        currentColor = Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));

        curPaint = new Paint();
        curPaint.setAntiAlias(true);
        curPaint.setColor(currentColor);
        curPaint.setStyle(Paint.Style.STROKE);
        curPaint.setStrokeJoin(Paint.Join.ROUND);
        curPaint.setStrokeWidth(STROKE_WIDTH);
        curPaint.setStrokeCap(Paint.Cap.ROUND);

        scaleMatrix = new Matrix();

        mPath = new PaintPath(curPaint);
        mPaths = new PaintStack();
        redoPaths = new PaintStack();
        mPaths.push(mPath);

        setWillNotDraw(false);
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
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        for (PaintPath p: mPaths) {
            canvas.drawPath(p, p.getPaint());
        }
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
        mPath = new PaintPath(currentStyle());
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

        mPath = new PaintPath(currentStyle());
        mPaths.push(mPath);

        invalidate();

        mBitmap.recycle();
        mBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mBitmap);
        buildDrawingCache(true);
    }

    public void changeColor(int color) {
        currentColor = color;
        curPaint.setColor(currentColor);
    }

    private PaintPath getLatestPath() {
        try {
            return mPaths.peek();
        } catch (EmptyStackException e) {
            return null;
        }
    }

    public float getBrushWidth() { return STROKE_WIDTH; }

    public void setBrushWidth(float width) {
        STROKE_WIDTH = width;
        curPaint.setStrokeWidth(width);
    }

    public Bitmap getBitmap() {
        return Bitmap.createBitmap(getDrawingCache(true));
    }

    public void undo() {
        PaintPath path = getLatestPath();
        if (path != null) {
            PaintPath redo = new PaintPath(path.getPaint());
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

    public Paint currentStyle() {
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(currentColor);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeWidth(STROKE_WIDTH);
        paint.setStrokeCap(Paint.Cap.ROUND);
        return paint;
    }
}