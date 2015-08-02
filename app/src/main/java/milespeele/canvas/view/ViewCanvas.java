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
import android.support.v4.view.MotionEventCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

import java.util.Random;

import milespeele.canvas.paint.PaintPath;
import milespeele.canvas.paint.PaintStack;
import milespeele.canvas.paint.PaintStyles;

/**
 * Created by milespeele on 7/2/15.
 */
public class ViewCanvas extends View {

    private static float STROKE_WIDTH = 5f;
    private boolean shouldErase = false;
    private boolean shouldRedraw = false;
    private boolean shouldInk = false;
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
    private ImageView eraser;
    private ImageView ink;

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
        setDrawingCacheQuality(DRAWING_CACHE_QUALITY_HIGH);
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
            shouldRedraw = false;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float eventX = event.getX();
        float eventY = event.getY();
        int actionMasked = MotionEventCompat.getActionMasked(event);

        switch (actionMasked & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_POINTER_DOWN:
                onTouchDown(event, eventX, eventY);
                break;

            case MotionEvent.ACTION_MOVE:
                onTouchMove(event, eventX, eventY);
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
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

    private void onTouchDown(MotionEvent event, float eventX, float eventY) {
        lastTouchX = eventX;
        lastTouchY = eventY;
        mPath = new PaintPath(currentStyle());
        mPaths.push(mPath);
        mPath.moveTo(eventX, eventY);

        setInkPosition(event, eventX, eventY);
        setEraserPosition(event, eventX, eventY);
    }

    private void onTouchMove(MotionEvent event, float eventX, float eventY) {
        resetDirtyRect(eventX, eventY);
        setInkPosition(event, eventX, eventY);
        setEraserPosition(event, eventX, eventY);

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

    private void onTouchUp(MotionEvent event, float eventX, float eventY) {
        setEraserPosition(event, eventX, eventY);
        setInkPosition(event, eventX, eventY);
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

    private void setEraserPosition(MotionEvent event, float eventX, float eventY) {
        if (shouldErase && eraser != null) {
            eraser.setTranslationX(eventX);
            eraser.setTranslationY(eventY);

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    eraser.setVisibility(View.VISIBLE);
                    break;
                case MotionEvent.ACTION_UP:
                    eraser.setVisibility(View.GONE);
                    break;
            }
        }
    }

    private void setInkPosition(MotionEvent event, float eventX, float eventY) {
        if (shouldInk && ink != null) {
            if (eventsInRange(eventX, eventY)) {
                int color = mBitmap.getPixel((int) eventX, (int) eventY);
                ink.setBackgroundColor(color);

                ink.setTranslationX(eventX);
                ink.setTranslationY(eventY);

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        ink.setVisibility(View.VISIBLE);
                        break;
                    case MotionEvent.ACTION_UP:
                        ink.setVisibility(View.GONE);
                        shouldInk = false;
                        if (color != currentBackgroundColor && currentBackgroundColor != -1)  {
                            curPaint.setColor(color);
                        } else {
                            curPaint.setColor(currentStrokeColor);
                        }
                        break;
                }
            }
        }
    }

    private boolean eventsInRange(float eventX, float eventY) {
        int x = Math.round(eventX), y = Math.round(eventY);
        return (x >= 0 && x <= mBitmap.getWidth() &&
                (y >= 0 && y <= mBitmap.getHeight()));
    }

    public void changeToEraser(ImageView eraser) {
        this.eraser = eraser;
        shouldErase = true;
        shouldInk = false;
    }

    public void showInk(ImageView ink) {
        this.ink = ink;
        shouldErase = false;
        shouldInk = true;
        shouldRedraw = false;
        curPaint.setColor(Color.TRANSPARENT);
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
        shouldInk = false;
        shouldErase = false;
        shouldRedraw = false;
        currentStrokeColor = color;
        curPaint.setColor(currentStrokeColor);
    }

    public float getBrushWidth() { return STROKE_WIDTH; }

    public void setBrushWidth(float width) {
        shouldInk = false;
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

    private Paint currentStyle() {
        if (shouldErase) {
            return (curPaint = PaintStyles.eraserPaint(currentBackgroundColor, STROKE_WIDTH));
        } else {
            return new Paint(curPaint);
        }
    }
}