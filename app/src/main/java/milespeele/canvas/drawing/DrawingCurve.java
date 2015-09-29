package milespeele.canvas.drawing;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.os.SystemClock;

import java.util.Random;

import milespeele.canvas.paint.PaintStyles;
import milespeele.canvas.util.EnumStore;
import milespeele.canvas.util.Logg;
import milespeele.canvas.view.ViewCanvas;

/**
 * Created by mbpeele on 9/25/15.
 */
public class DrawingCurve implements EnumStore.EnumListener {

    private final RectF inkRect = new RectF();
    private final RectF dirtyRect = new RectF();
    private Bitmap mBitmap;
    private Canvas mCanvas;
    private DrawingPoints currentPoints, currentPointsHistory;
    private DrawingHistory redoPoints, allPoints;
    private Paint mPaint, inkPaint;
    private Random random;
    private ViewCanvas.State mState;

    private static final float VELOCITY_FILTER_WEIGHT = 0.2f;
    private static float STROKE_WIDTH = 10f;
    private static final float POINT_TOLERANCE = 5f;
    private int width, height;
    private float lastTouchX, lastTouchY, lastWidth, lastVelocity;
    private int[] rainbow;
    private int currentStrokeColor, currentBackgroundColor;

    public DrawingCurve(int w, int h) {
        width = w;
        height = h;

        createInkRect(w, h);
        mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mBitmap);

        currentPoints = new DrawingPoints();
        currentPointsHistory = new DrawingPoints();
        allPoints = new DrawingHistory();
        redoPoints = new DrawingHistory();

        random = new Random();
        rainbow = getRainbowColors();

        mPaint = PaintStyles.normal(currentStrokeColor, STROKE_WIDTH);
        inkPaint = PaintStyles.normal(currentStrokeColor, STROKE_WIDTH);
        inkPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        inkPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP));
    }

    public void resize(int w, int h) {
//        width = w;
//        height = h;
//
//        Bitmap bitmap = Bitmap.createScaledBitmap(mBitmap, w, h, true);
//        mBitmap.recycle();
//        mBitmap = bitmap;
//        mCanvas = new Canvas(mBitmap);
    }

    public void reset() {
        mCanvas.drawColor(currentBackgroundColor, PorterDuff.Mode.CLEAR);
    }

    public void reset(int color) {
        mCanvas.drawColor(color, PorterDuff.Mode.CLEAR);
    }

    public void hardReset(int color) {
        resetRect();

        currentPoints.clear();
        currentPointsHistory.clear();
        allPoints.clear();
        redoPoints.clear();

        lastTouchX = 0;
        lastTouchY = 0;
        lastWidth = 0;
        lastVelocity = 0;

        mBitmap.recycle();
        mBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mBitmap);

        reset(color);
    }

    public void setPaint(Paint paint) {
        mPaint = paint;
    }

    public void drawColorToInternalCanvas(int color) {
        mCanvas.drawColor(color);
    }

    public void drawBitmapToInternalCanvas(Bitmap bitmap) {
        if (bitmap != null) {
            mCanvas.drawBitmap(bitmap, 0, 0, null);
        }
    }

    public void drawToViewCanvas(Canvas canvas) {
        canvas.drawBitmap(mBitmap, 0, 0, null);

        if (mState == ViewCanvas.State.INK) {
            canvas.drawRect(inkRect, inkPaint);
        }
    }

    public int getPixel(float eventX, float eventY) {
        return mBitmap.getPixel(Math.round(eventX), Math.round(eventY));
    }

    public void onTouchUp(float eventX, float eventY) {
        lastTouchX = eventX;
        lastTouchY = eventY;
        lastVelocity = 0;
        lastWidth = 0;

        addPoint(eventX, eventY);

        allPoints.push(currentPointsHistory);
        currentPoints.clear();
        currentPointsHistory.clear();
    }

    public void addPoint(float x, float y) {
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
            currentPointsHistory.add(new DrawingPoint(x, y, x, y,
                    mPaint.getStrokeWidth(), mPaint.getColor(), mPaint));
        } else {
            DrawingPoint currentPoint = new DrawingPoint(x, y, SystemClock.currentThreadTimeMillis());
            currentPoints.add(currentPoint);
            drawLine(prevPoint, currentPoint.midPoint(prevPoint), currentPoint);
        }
    }

    private void drawLine(DrawingPoint previous, DrawingPoint mid, DrawingPoint current) {
        float velocity =  VELOCITY_FILTER_WEIGHT * current.velocityFrom(previous)
                + (1 - VELOCITY_FILTER_WEIGHT) * lastVelocity;
        float strokeWidth = STROKE_WIDTH - velocity;
        float diff = strokeWidth - lastWidth;

        float xa, xb, ya, yb, x, y;
        for (float i = 0; i < 1; i += .01) {
            xa = previous.getMidX(mid, i);
            ya = previous.getMidY(mid, i);

            xb = mid.getMidX(current, i);
            yb = mid.getMidY(current, i);

            x = xa + ((xb - xa) * i);
            y = ya + ((yb - ya) * i);

            if (mState != ViewCanvas.State.ERASE) {
                mPaint.setStrokeWidth(Math.abs(lastWidth * 2 + diff * i));
            }

            if (mState == ViewCanvas.State.RAINBOW) {
                mPaint.setColor(rainbow[random.nextInt(rainbow.length)]);
            }

            currentPointsHistory.add(new DrawingPoint(previous.x, previous.y, x, y,
                    mPaint.getStrokeWidth(), mPaint.getColor(), mPaint));
            mCanvas.drawLine(previous.x, previous.y, x, y, mPaint);
        }


        if (strokeWidth != Float.POSITIVE_INFINITY && strokeWidth != Float.NEGATIVE_INFINITY) {
            lastWidth = strokeWidth;
        }

        if (velocity != Float.POSITIVE_INFINITY && velocity != Float.NEGATIVE_INFINITY) {
            lastVelocity = velocity;
        }
    }

    public boolean redo(Bitmap bitmap) {
        if (!redoPoints.isEmpty()) {
            reset();

            DrawingPoints points = redoPoints.pop();
            allPoints.push(points);

            drawBitmapToInternalCanvas(bitmap);

            redraw();
            return true;
        }
        return false;
    }

    public boolean undo(Bitmap bitmap) {
        if (!allPoints.isEmpty()) {
            reset();

            DrawingPoints points = allPoints.pop();
            redoPoints.push(points);

            drawBitmapToInternalCanvas(bitmap);

            redraw();
            return true;
        }
        return false;
    }

    public void redraw() {
        resetRect();
        for (DrawingPoints points: allPoints) {
            for (DrawingPoint point: points) {
                if (point.paint != null) {
                    mPaint.set(point.paint);
                }
                mPaint.setColor(point.color);
                mPaint.setStrokeWidth(point.width);
                if (point.fromX == point.toX) {
                    mCanvas.drawPoint(point.fromX, point.fromY, mPaint);
                } else {
                    mCanvas.drawLine(point.fromX, point.fromY, point.toX, point.toY, mPaint);
                }
                updateRect(point.toX, point.toY);
            }
        }
    }

    public void resetRect() {
        resetRect(0, 0);
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

    public int[] getDirtyRectPos() {
        return new int[] {
                Math.round(dirtyRect.left - STROKE_WIDTH / 2),
                Math.round(dirtyRect.top - STROKE_WIDTH / 2),
                Math.round(dirtyRect.right + STROKE_WIDTH / 2),
                Math.round(dirtyRect.bottom + STROKE_WIDTH / 2)
        };
    }

    public int[] getInkRectPos() {
        return new int[] {
                Math.round(inkRect.left - STROKE_WIDTH / 2),
                Math.round(inkRect.top - STROKE_WIDTH / 2),
                Math.round(inkRect.right + STROKE_WIDTH /2),
                Math.round(inkRect.bottom + STROKE_WIDTH / 2)
        };
    }

    public Paint getPaint() { return mPaint; }

    public int getWidth() { return width; }

    public int getHeight() { return height; }

    public Bitmap getBitmap() { return mBitmap; }

    public float getStaticStrokeWidth() { return STROKE_WIDTH; }

    public void setStaticStrokeWidth(float width) { STROKE_WIDTH = width; }

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

    public void setPaintColors(int currentStrokeColor, int currentBackgroundColor) {
        this.currentStrokeColor = currentStrokeColor;
        this.currentBackgroundColor = currentBackgroundColor;

        mPaint.setColor(currentStrokeColor);
    }

    public void setInkPaintColor(int color) { inkPaint.setColor(color); }

    public void setPaintThickness(float thickness) {
        mPaint.setStrokeWidth(thickness);
    }

    public void setPaintAlpha(int opacity) {
        mPaint.setAlpha(opacity);
    }

    public void setPaintColor(int color) {
        currentStrokeColor = color;
        mPaint.setColor(currentStrokeColor);
    }

    private void createInkRect(int w, int h) {
        if (w < h) {
            inkRect.left = w / 40;
            inkRect.top = w / 40;
            inkRect.right = w / 5;
            inkRect.bottom = w / 5;
        } else {
            inkRect.left = h / 40;
            inkRect.top = h / 40;
            inkRect.right = h / 5;
            inkRect.bottom = h / 5;
        }
    }

    @Override
    public void onValueChanged(ViewCanvas.State newValue) {
        mState = newValue;

        switch (mState) {
            case ERASE:
                mPaint = PaintStyles.erase(currentBackgroundColor, 20f);
                break;
            case DRAW:
                mPaint.setColor(currentStrokeColor);
                break;
            case INK:
                inkPaint.setColor(currentStrokeColor);
                break;
        }
    }
}
