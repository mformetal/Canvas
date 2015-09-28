package milespeele.canvas.drawing;

import android.animation.Animator;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Handler;
import android.os.SystemClock;
import android.view.View;

import java.util.Random;
import java.util.Stack;

import javax.inject.Inject;

import de.greenrobot.event.EventBus;
import milespeele.canvas.MainApp;
import milespeele.canvas.R;
import milespeele.canvas.event.EventColorChosen;
import milespeele.canvas.util.AbstractAnimatorListener;
import milespeele.canvas.util.Logg;

/**
 * Created by mbpeele on 9/25/15.
 */
public class DrawingCurve {

    private Bitmap mBitmap;
    private Canvas mCanvas;
    private Stack<DrawingPoint> currentPoints;
    private Paint mPaint;
    private RectF dirtyRect;
    private Random random;
    private Path mPath;

    private static final float VELOCITY_FILTER_WEIGHT = 0.2f;
    private static float STROKE_WIDTH = 10f;
    private static final float POINT_TOLERANCE = 5f;
    private int width, height;
    private float lastTouchX, lastTouchY, lastWidth, lastVelocity;
    private int[] rainbow;

    public DrawingCurve(int w, int h) {
        width = w;
        height = h;

        mPath = new Path();
        dirtyRect = new RectF();
        mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mBitmap);

        currentPoints = new Stack<>();

        random = new Random();
        rainbow = getRainbowColors();
    }

    public int[] getRainbowColors() {
        return new int[] {
                    Color.RED,
                    Color.parseColor("#FF7F00"),
                    Color.YELLOW,
                    Color.GREEN,
                    Color.BLUE,
                    Color.parseColor("#4B0082"),
                    Color.parseColor("#8B00FF")
        };
    }

    public void resize(int w, int h) {
        width = w;
        height = h;

        Bitmap bitmap = Bitmap.createScaledBitmap(mBitmap, w, h, true);
        mBitmap.recycle();
        mBitmap = bitmap;
        mCanvas = new Canvas(mBitmap);
    }

    public void reset() {
        mBitmap.recycle();
        mBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mBitmap);
    }

    public void hardReset() {
        resetRect(0, 0);
        currentPoints.clear();

        lastTouchX = 0;
        lastTouchY = 0;
        lastWidth = 0;
        lastVelocity = 0;

        mBitmap.recycle();
        mBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mBitmap);
    }

    public void setPaint(Paint paint) {
        mPaint = paint;
        if (paint.getShader() != null) {
            paint.setShader(null);
        }
    }

    public void drawColorToInternalCanvas(int color) {
        mCanvas.drawColor(color);
    }

    public void drawBitmapToInternalCanvas(Bitmap bitmap) {
        if (bitmap != null) {
            mCanvas.drawBitmap(bitmap, 0, 0, null);
        }
    }

    public void drawInternalBitmapToCanvas(Canvas canvas) {
        canvas.drawBitmap(mBitmap, 0, 0, null);
    }

    public int getPixel(float eventX, float eventY) {
        return mBitmap.getPixel(Math.round(eventX), Math.round(eventY));
    }

    public void onTouchUp(float eventX, float eventY) {
        lastTouchX = eventX;
        lastTouchY = eventY;
        lastVelocity = 0;
        lastWidth = 0;
        addPoint(eventX, eventY, false);
        currentPoints.clear();
    }

    public void addPoint(float x, float y, boolean toRainbow) {
        DrawingPoint prevPoint = null;
        if (!currentPoints.isEmpty()) {
            prevPoint = currentPoints.peek();
            if (Math.abs(prevPoint.x - x) < POINT_TOLERANCE && Math.abs(prevPoint.y - y) < POINT_TOLERANCE) {
                return;
            }
        }

        mPath.moveTo(x, y);

        updateRect(x, y);

        if (prevPoint == null) {
            currentPoints.push(new DrawingPoint(x, y, SystemClock.currentThreadTimeMillis()));
            mCanvas.drawPoint(x, y, mPaint);
        } else {
            DrawingPoint currentPoint = new DrawingPoint(x, y, SystemClock.currentThreadTimeMillis());
            currentPoints.push(currentPoint);
            drawLine(prevPoint, currentPoint.midPoint(prevPoint), currentPoint, toRainbow);
        }
    }

    public void addPointToErase(float x, float y) {
        DrawingPoint prevPoint = null;
        if (!currentPoints.isEmpty()) {
            prevPoint = currentPoints.peek();
            if (Math.abs(prevPoint.x - x) < POINT_TOLERANCE && Math.abs(prevPoint.y - y) < POINT_TOLERANCE) {
                return;
            }
        }

        updateRect(x, y);

        if (prevPoint == null) {
            currentPoints.add(new DrawingPoint(x, y, SystemClock.currentThreadTimeMillis()));
            mCanvas.drawPoint(x, y, mPaint);
        } else {
            DrawingPoint currentPoint = new DrawingPoint(x, y, SystemClock.currentThreadTimeMillis());
            currentPoints.add(currentPoint);
            mCanvas.drawLine(prevPoint.x, prevPoint.y, currentPoint.x, currentPoint.y, mPaint);
        }
    }

    private void drawLine(DrawingPoint previous, DrawingPoint mid, DrawingPoint current, boolean toRainbow) {
        float prevWidth = mPaint.getStrokeWidth();
        float velocity =  VELOCITY_FILTER_WEIGHT * current.velocityFrom(previous) + (1 - VELOCITY_FILTER_WEIGHT) * lastVelocity;
        float strokeWidth = STROKE_WIDTH - velocity;
        float diff = strokeWidth - lastWidth;

        float xa, xb, ya, yb, x, y;
        for (float i = 0; i < 1; i += .1) {
            xa = previous.getMidX(mid, i);
            ya = previous.getMidY(mid, i);

            xb = mid.getMidX(current, i);
            yb = mid.getMidY(current, i);

            x = xa + ((xb - xa) * i);
            y = ya + ((yb - ya) * i);

            mPaint.setStrokeWidth(Math.abs(lastWidth + diff * i));
            if (toRainbow) {
                mPaint.setColor(rainbow[random.nextInt(rainbow.length)]);
            }
            mPath.lineTo(x, y);
            mCanvas.drawLine(previous.x, previous.y, x, y, mPaint);
        }

        lastWidth = strokeWidth;
        lastVelocity = velocity;

        mPaint.setStrokeWidth(prevWidth);
    }

    public boolean redo() {
//        if (!redoPoints.isEmpty()) {
//            reset();
//
//            Stack<DrawingPoint> redone = redoPoints.pop();
//            allPoints.push(redone);
//
//            drawPoints();
//            return true;
//        }
//        return false;
        return false;
    }

    public boolean undo() {
        return false;
    }

    public void resetRect(float x, float y) {
        dirtyRect.left = Math.min(lastTouchX, x);
        dirtyRect.right = Math.max(lastTouchX, x);
        dirtyRect.top = Math.min(lastTouchY, y);
        dirtyRect.bottom = Math.max(lastTouchY, y);
    }

    public void updateRect(float x, float y) {
        if (dirtyRect.left < x) {
            dirtyRect.right = x;
        } else {
            dirtyRect.right = dirtyRect.left;
            dirtyRect.left = x;
        }

        if (dirtyRect.top < y) {
            dirtyRect.bottom = y;
        } else {
            dirtyRect.bottom = dirtyRect.top;
            dirtyRect.top = y;
        }
    }

    public int getLeft() {
        return Math.round(dirtyRect.left - STROKE_WIDTH / 2);
    }

    public int getTop() {
        return Math.round(dirtyRect.top - STROKE_WIDTH / 2);
    }

    public int getRight() {
        return Math.round(dirtyRect.right + STROKE_WIDTH / 2);
    }

    public int getBottom() {
        return Math.round(dirtyRect.bottom + STROKE_WIDTH / 2);
    }

    public int getWidth() { return width; }

    public int getHeight() { return height; }

    public Bitmap getBitmap() { return mBitmap; }

    public float getStaticStrokeWidth() { return STROKE_WIDTH; }

    public void setStaticStrokeWidth(float width) { STROKE_WIDTH = width; }

}
