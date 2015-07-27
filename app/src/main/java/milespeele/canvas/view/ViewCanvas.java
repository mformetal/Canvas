package milespeele.canvas.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.facebook.internal.Logger;

import java.util.EmptyStackException;
import java.util.Random;
import java.util.Stack;

import milespeele.canvas.paint.PaintPath;
import milespeele.canvas.paint.PaintStack;
import milespeele.canvas.paint.PaintStyles;
import milespeele.canvas.util.Logg;

/**
 * Created by milespeele on 7/2/15.
 */
public class ViewCanvas extends View {

    private static float STROKE_WIDTH = 5f;
    private static float HALF_STROKE_WIDTH = STROKE_WIDTH / 2;
    private boolean shouldErase = false;
    private boolean shouldRedraw = false;

    private int currentStrokeColor;
    private int currentBackgroundColor;
    private Paint curPaint;
    private Bitmap mBitmap;
    private Canvas mCanvas;
    private Matrix scaleMatrix;
    private float lastTouchX, lastTouchY;
    private final RectF dirtyRect = new RectF();
    private PaintPath mPath;
    private PaintStack mPaths;
    private PaintStack redoPaths;
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
//                if (shouldErase) {
//                    curPaint.setColor(mBitmap.getPixel((int) eventX, (int) eventY));
//                }
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
        currentBackgroundColor = color;
        for (PaintPath p: mPaths) {
            p.reset();
        }
        setBackgroundColor(color);
        setDrawingCacheBackgroundColor(color);
        shouldRedraw = true;
        invalidate();
    }

    public void clearCanvas() {
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

        currentBackgroundColor = Color.WHITE;
        setBackgroundColor(currentBackgroundColor);
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
            invalidate();
        }
    }

    public void redo() {
        if (!redoPaths.isEmpty()) {
            PaintPath redo = redoPaths.pop();
            mPaths.push(redo);
            shouldRedraw = true;
            invalidate();
        }
    }

    public Paint currentStyle() {
        if (shouldErase) {
            return PaintStyles.eraserPaint(currentBackgroundColor, STROKE_WIDTH);
        } else {
            shouldRedraw = false;
            return new Paint(curPaint);
        }
    }
}