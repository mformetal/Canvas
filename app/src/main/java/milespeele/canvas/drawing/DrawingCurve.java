package milespeele.canvas.drawing;

import android.animation.Animator;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.View;
import android.widget.LinearLayout;

import java.util.Stack;

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
    private Rect dirtyRect;

    private static float STROKE_WIDTH = 5f;
    private static final float VELOCITY_FILTER_WEIGHT = 0.2f;
    private static final float TOLERANCE = 5f;
    private int width, height;
    private float lastTouchX, lastTouchY;
    private float lastVelocity, lastWidth;

    public DrawingCurve(int w, int h) {
        width = w;
        height = h;

        dirtyRect = new Rect();
        mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mBitmap);

        currentPoints = new Stack<>();
    }

    public void reset() {
        mBitmap.recycle();
        mBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mBitmap);
    }

    public void hardReset() {
        currentPoints.clear();
        mBitmap.recycle();
        mBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mBitmap);
    }

    public void hardResetWithAnimatedColor(View view, int colorFrom, int colorTo) {
        hardReset();

        ObjectAnimator background =
                ObjectAnimator.ofObject(view, "backgroundColor", new ArgbEvaluator(),
                        colorFrom, colorTo);
        background.setDuration(750);
        background.addListener(new AbstractAnimatorListener() {

            @Override
            public void onAnimationStart(Animator animation) {
                view.setFocusable(false);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                view.setFocusable(true);
                mCanvas.drawColor(colorTo);
            }
        });

        background.start();
    }

    public void setPaint(Paint paint) {
        mPaint = paint;
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
        currentPoints.clear();
    }

    public void addPoint(float x, float y, float time) {
        DrawingPoint prevPoint = null;
        if (!currentPoints.isEmpty()) {
            prevPoint = currentPoints.peek();
            if (Math.abs(prevPoint.x - x) < TOLERANCE && Math.abs(prevPoint.y - y) < TOLERANCE) {
                return;
            }
        }

        if (prevPoint == null) {
            currentPoints.add(new DrawingPoint(x, y, time));
            mCanvas.drawPoint(x, y, mPaint);
        } else {
            DrawingPoint currentPoint = new DrawingPoint(x, y, time);
            currentPoints.add(currentPoint);
            drawLine(prevPoint, currentPoint);
        }
    }

    private void drawLine(DrawingPoint previous, DrawingPoint current) {
        float velocity = current.velocityFrom(previous);
        float strokeWidth = STROKE_WIDTH - velocity * (1 - VELOCITY_FILTER_WEIGHT) * lastVelocity;

        DrawingPoint mid = current.midPoint(previous);
        mCanvas.drawLine(previous.x, previous.y, mid.x, mid.y, mPaint);
        mCanvas.drawLine(mid.x, mid.y, current.x, current.y, mPaint);

//        float xa, xb, ya, yb, x, y;
//        for (int i = 0; i < 1; i += .1) {
//            xa = getPt(previous.x, mid.x, i);
//            ya = getPt(previous.y, mid.y, i);
//            xb = getPt(mid.x, current.x, i);
//            yb = getPt(mid.y, current.y, i);
//
//            x = getPt(xa, xb, i);
//            y = getPt(ya, yb, i);

//            mPaint.setStrokeWidth(lastWidth * strokeWidth * i);
//            mCanvas.drawLine(previous.x, previous.y, x, y, mPaint);
//        }

        mPaint.setStrokeWidth(STROKE_WIDTH);

        lastVelocity = velocity;
        lastWidth = strokeWidth;
    }

    private float getPt(float n1, float n2, float perc) {
        float diff = n2 - n1;
        return n1 + (diff * perc);
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
//        if (!allPoints.isEmpty()) {
//            reset();
//
//            Stack<DrawingPoint> undone = allPoints.pop();
//            redoPoints.push(undone);
//
//            drawPoints();
//            return true;
//        } else {
//            return false;
//        }
        return false;
    }

    public void resetRect(float x, float y) {
        dirtyRect.left = (int) Math.min(lastTouchX, x);
        dirtyRect.right = (int) Math.max(lastTouchX, x);
        dirtyRect.top = (int) Math.min(lastTouchY, y);
        dirtyRect.bottom = (int) Math.max(lastTouchY, y);
    }

    public void updateRect(float x, float y) {
        int roundedX = Math.round(x);
        int roundedY = Math.round(y);
        if (dirtyRect.left < roundedX) {
            dirtyRect.right = roundedX;
        } else {
            dirtyRect.right = dirtyRect.left;
            dirtyRect.left = roundedX;
        }

        if (dirtyRect.top < roundedY) {
            dirtyRect.bottom = roundedY;
        } else {
            dirtyRect.bottom = dirtyRect.top;
            dirtyRect.top = roundedY;
        }
    }

    public Rect getDirtyRect() { return dirtyRect; }

    public int getWidth() { return width; }

    public int getHeight() { return height; }

    public Bitmap getBitmap() { return mBitmap; }

    public float getStaticStrokeWidth() { return STROKE_WIDTH; }

    public void setStaticStrokeWidth(float width) { STROKE_WIDTH = width; }
}
