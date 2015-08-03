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
import milespeele.canvas.paint.Point;

/**
 * Created by milespeele on 7/2/15.
 */
public class ViewCanvas extends View {

    private static float STROKE_WIDTH = 8f;
    private float lastWidth;
    private float lastVelocity;
    private static final float VELOCITY_FILTER_WEIGHT = 0.2f;
    private boolean shouldErase = false;
    private boolean shouldRedraw = false;
    private boolean shouldInk = false;
    private int currentStrokeColor;
    private int currentBackgroundColor;
    private float lastTouchX, lastTouchY;

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
    private Point previousPoint;
    private Point startPoint;
    private Point currentPoint;

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
        for (PaintPath p: mPaths) {
            p.reset();
        }
        mPaths.clear();

        mPath = new PaintPath(currentStyle());
        mPaths.push(mPath);

        scaleMatrix.reset();
        scaleMatrix.setScale(w, h);

        if (mBitmap != null) {
            mBitmap.recycle();
        }
        mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mBitmap);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (shouldRedraw) {
            for (PaintPath p: mPaths) {
                canvas.drawPath(p, p.getPaint());
            }
//            shouldRedraw = false;
        } else {
            canvas.drawBitmap(mBitmap, 0, 0, null);
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
        if (!shouldInk) {
            lastTouchX = eventX;
            lastTouchY = eventY;
            currentPoint = new Point(event.getX(), event.getY(), System.currentTimeMillis());
            previousPoint = currentPoint;
            startPoint = previousPoint;
            mPath = new PaintPath(currentStyle());
            mPaths.push(mPath);
            mPath.moveTo(eventX, eventY);
        }

        setInkPosition(event, eventX, eventY);
        setEraserPosition(event, eventX, eventY);
    }

    private void onTouchMove(MotionEvent event, float eventX, float eventY) {
        resetDirtyRect(eventX, eventY);
        setInkPosition(event, eventX, eventY);
        setEraserPosition(event, eventX, eventY);

        if (!shouldInk) {
            for (int i = 0; i < event.getHistorySize(); i++) {
                float historicalX = event.getHistoricalX(i);
                float historicalY = event.getHistoricalY(i);
                expandDirtyRect(historicalX, historicalY);
                startPoint = previousPoint;
                previousPoint = currentPoint;
                currentPoint = new Point(event.getX(), event.getY(), System.currentTimeMillis());

                // Calculate the velocity between the current point to the previous point
                float velocity = currentPoint.velocityFrom(previousPoint);

                // A simple lowpass filter to mitigate velocity aberrations.
                velocity = VELOCITY_FILTER_WEIGHT * velocity + (1 - VELOCITY_FILTER_WEIGHT) * lastVelocity;

                // Caculate the stroke width based on the velocity
                float strokeWidth = STROKE_WIDTH - velocity;


                // Draw line to the canvasBmp canvas.
                drawLine(mCanvas, curPaint, lastWidth, strokeWidth);

                // Tracker the velocity and the stroke width
                lastVelocity = velocity;
                lastWidth = strokeWidth;
                mPath.lineTo(historicalX, historicalY);
            }

            mPath.lineTo(eventX, eventY);
            mCanvas.drawPath(mPath, mPath.getPaint());
        }
    }

    private void onTouchUp(MotionEvent event, float eventX, float eventY) {
        setEraserPosition(event, eventX, eventY);
        setInkPosition(event, eventX, eventY);

        startPoint = previousPoint;
        previousPoint = currentPoint;
        currentPoint = new Point(event.getX(), event.getY(), System.currentTimeMillis());
        drawLine(mCanvas, curPaint, lastWidth, 0);
    }

    private void drawLine(Canvas canvas, Paint paint, float lastWidth, float currentWidth) {
        Point mid1 = previousPoint.midPoint(startPoint);
        Point mid2 = currentPoint.midPoint(previousPoint);
        draw(canvas, mid1, previousPoint, mid2, paint, lastWidth, currentWidth);
    }

    private void draw(Canvas canvas, Point p0, Point p1, Point p2, Paint paint, float lastWidth, float currentWidth) {
        float xa, xb, ya, yb, x, y;
        float different = (currentWidth - lastWidth);

        for (float i = 0; i < 1; i += 0.01) {
            xa = getPt(p0.x, p1.x, i);
            ya = getPt(p0.y, p1.y, i);
            xb = getPt(p1.x, p2.x, i);
            yb = getPt(p1.y, p2.y, i);

            x = getPt(xa, xb, i);
            y = getPt(ya, yb, i);

            // reset strokeWidth
            paint.setStrokeWidth(lastWidth + different * (i));
            canvas.drawPoint(x, y, paint);
        }
    }

    private float getPt(float n1, float n2, float perc) {
        float diff = n2 - n1;
        return n1 + (diff * perc);
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
                        if (color != currentBackgroundColor)  {
                            curPaint.setColor((color == 0) ? currentStrokeColor : color);
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
        if (ink != null) {
            ink.setVisibility(View.GONE);
        }
        this.eraser = eraser;
        eraser.setVisibility(View.VISIBLE);
        eraser.setX((float) getWidth() / 2);
        eraser.setY((float) getHeight() / 2);
        shouldErase = true;
        shouldInk = false;
    }

    public void changeToInk(ImageView ink) {
        if (eraser != null) {
            eraser.setVisibility(View.GONE);
        }
        this.ink = ink;
        ink.setX((float) getWidth() / 2);
        ink.setY((float) getHeight() / 2);
        ink.setBackgroundColor(mBitmap.getPixel(getWidth() / 2, getHeight() / 2));
        ink.setVisibility(View.VISIBLE);
        shouldInk = true;
        shouldRedraw = false;
    }

    public void changeColor(int color) {
        if (ink != null) {
            ink.setVisibility(View.GONE);
        }

        if (eraser != null) {
            eraser.setVisibility(View.GONE);
        }
        shouldInk = false;
        shouldErase = false;
        currentStrokeColor = color;
        curPaint.setColor(currentStrokeColor);
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
        mBitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
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
            float eraseStroke = STROKE_WIDTH * 5f;
            return (curPaint = PaintStyles.eraserPaint(currentBackgroundColor, eraseStroke));
        } else {
            return new Paint(curPaint);
        }
    }
}