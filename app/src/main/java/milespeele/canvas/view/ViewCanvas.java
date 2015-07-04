package milespeele.canvas.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;

import milespeele.canvas.util.Logger;

/**
 * Created by milespeele on 7/2/15.
 */
public class ViewCanvas extends View {

    private Bitmap mBitmap;
    private Canvas mCanvas;
    Context context;
    private float mX, mY;
    private static final float TOLERANCE = 5;
    private ArrayList<PaintPath> mPaths;
    private Matrix scaleMatrix;

    public ViewCanvas(Context c, AttributeSet attrs) {
        super(c, attrs);
        context = c;

        scaleMatrix = new Matrix();

        mPaths = new ArrayList<>();
        mPaths.add(new PaintPath(generatePaintWithColor(Color.BLACK)));
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        scaleMatrix.reset();
        scaleMatrix.setScale(w, h);

        mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mBitmap);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Logger.log("SIZE: " + mPaths.size());
        for (PaintPath p: mPaths) {
            canvas.drawPath(p, p.getPaint());
        }
    }

    private void startTouch(float x, float y) {
        getLatestPath().moveTo(x, y);
        mX = x;
        mY = y;
    }

    private void moveTouch(float x, float y) {
        float dx = Math.abs(x - mX);
        float dy = Math.abs(y - mY);
        if (dx >= TOLERANCE || dy >= TOLERANCE) {
            getLatestPath().quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2);
            mX = x;
            mY = y;
        }
    }

    private void upTouch() {
        getLatestPath().lineTo(mX, mY);
        mPaths.add(getLatestPath());
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                startTouch(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                moveTouch(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                upTouch();
                invalidate();
                break;
        }
        return true;
    }

    public void clearCanvas() {
        for (PaintPath p: mPaths) {
            p.reset();
        }
        invalidate();
    }

    public void changeColor(int color) {
        mPaths.add(new PaintPath(generatePaintWithColor(color)));
    }

    private PaintPath getLatestPath() {
        return mPaths.get(mPaths.size() - 1);
    }

    private Paint generatePaintWithColor(int color) {
        Paint mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setColor(color);
        mPaint.setStrokeWidth(4f);
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
