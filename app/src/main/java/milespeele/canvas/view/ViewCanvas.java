package milespeele.canvas.view;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
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

import java.util.Random;

import milespeele.canvas.paint.PaintPath;
import milespeele.canvas.paint.PaintStack;
import milespeele.canvas.paint.PaintStyles;
import milespeele.canvas.util.Logg;

/**
 * Created by milespeele on 7/2/15.
 */
public class ViewCanvas extends View {

    private static float STROKE_WIDTH = 5f;
    private boolean shouldErase = false;
    private boolean shouldRedraw = false;
    private int currentStrokeColor;
    private int currentBackgroundColor;
    private float lastTouchX, lastTouchY;
    private int width, height;

    private final RectF dirtyRect = new RectF();
    private PaintPath mPath;
    private PaintStack mPaths;
    private PaintStack redoPaths;
    private Paint curPaint;
    private Bitmap mBitmap;
    private Canvas mCanvas;
    private Matrix scaleMatrix;

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
        currentStrokeColor = Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));
        currentBackgroundColor = Color.WHITE;

        curPaint = PaintStyles.randomStyle(currentStrokeColor, STROKE_WIDTH);

        mPath = new PaintPath(curPaint);
        mPaths = new PaintStack();
        redoPaths = new PaintStack();
        mPaths.push(mPath);

        scaleMatrix = new Matrix();

        setWillNotDraw(false);
        setSaveEnabled(true);
        setBackgroundColor(currentBackgroundColor);
        setLayerType(LAYER_TYPE_HARDWARE, null);
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
        if (shouldRedraw) {
            for (PaintPath p: mPaths) {
                canvas.drawPath(p, p.getPaint());
            }
        } else {
            canvas.drawBitmap(mBitmap, 0, 0, null);
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
        }

        invalidate(Math.round(dirtyRect.left - STROKE_WIDTH / 2),
                Math.round(dirtyRect.top - STROKE_WIDTH / 2),
                Math.round(dirtyRect.right + STROKE_WIDTH / 2),
                Math.round(dirtyRect.bottom + STROKE_WIDTH / 2));

        lastTouchX = eventX;
        lastTouchY = eventY;
        return true;
    }

    private void onTouchDown(float eventX, float eventY, float time) {
        lastTouchX = eventX;
        lastTouchY = eventY;
        mPath = new PaintPath(currentStyle());
        mPaths.push(mPath);
        mPath.moveTo(eventX, eventY);
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
        mCanvas.drawPath(mPath, mPath.getPaint());
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

    public void changeToEraser() {
        shouldErase = true;
    }

    public void fillCanvas(int color) {
        shouldErase = false;
        for (PaintPath p: mPaths) {
            p.reset();
        }
        mPaths.clear();

        mPath = new PaintPath(currentStyle());
        mPaths.push(mPath);

        mBitmap.recycle();
        mBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mBitmap);

        final ObjectAnimator backgroundColorAnimator = ObjectAnimator.ofObject(this,
                "backgroundColor",
                new ArgbEvaluator(),
                currentBackgroundColor,
                color);
        backgroundColorAnimator.setDuration(450);
        backgroundColorAnimator.start();

        currentBackgroundColor = color;
        setDrawingCacheBackgroundColor(currentBackgroundColor);
        invalidate();
    }

    public void changeColor(int color) {
        shouldErase = false;
        currentStrokeColor = color;
        curPaint.setColor(currentStrokeColor);
    }

    public float getBrushWidth() { return STROKE_WIDTH; }

    public void setBrushWidth(float width) {
        STROKE_WIDTH = width;
        curPaint.setStrokeWidth(width);
    }

    public Bitmap getBitmap() {
        return mBitmap;
    }

    public void undo() {
        if (!mPaths.isEmpty()) {
            PaintPath undo = mPaths.pop();
            redoPaths.push(undo);
            shouldRedraw = true;
            invalidate(undo.getLeft(), undo.getTop(), undo.getRight(), undo.getBottom());
        }
    }

    public void redo() {
        if (!redoPaths.isEmpty()) {
            PaintPath redo = redoPaths.pop();
            mPaths.push(redo);
            shouldRedraw = true;
            invalidate(redo.getLeft(), redo.getTop(), redo.getRight(), redo.getBottom());
        }
    }

    public Paint currentStyle() {
        if (shouldErase) {
            return (curPaint = PaintStyles.eraserPaint(currentBackgroundColor, STROKE_WIDTH));
        } else {
            return new Paint(curPaint);
        }
    }
}