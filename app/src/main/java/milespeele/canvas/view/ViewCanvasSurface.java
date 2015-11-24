package milespeele.canvas.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.RectF;
import android.os.Build;
import android.support.v4.view.MotionEventCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import com.squareup.picasso.Cache;

import java.util.ArrayList;

import javax.inject.Inject;

import milespeele.canvas.drawing.DrawingCurve;

/**
 * Created by Miles Peele on 10/2/2015.
 */
public class ViewCanvasSurface extends SurfaceView implements SurfaceHolder.Callback, View.OnTouchListener {

    private DrawingCurve drawingCurve;

    private final RectF dirtyRect = new RectF();

    public ViewCanvasSurface(Context context) {
        super(context);
        init();
    }

    public ViewCanvasSurface(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ViewCanvasSurface(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public ViewCanvasSurface(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    public void init() {
        setLayerType(LAYER_TYPE_NONE, null);
        setWillNotDraw(false);
        setSaveEnabled(true);
        setOnTouchListener(this);
        getHolder().addCallback(this);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        drawingCurve = new DrawingCurve(getContext(), w, h);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawBitmap(drawingCurve.getBitmap(), 0, 0, null);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        int actionMasked = MotionEventCompat.getActionMasked(event);

        switch (actionMasked & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_POINTER_DOWN:
                onTouchDown(event);
                break;

            case MotionEvent.ACTION_MOVE:
                onTouchMove(event);
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
                onTouchUp(event);
                break;
        }

        invalidate(drawingCurve.getDirtyRect());

        return true;
    }

    public void onTouchDown(MotionEvent event) {
        if (event.getPointerCount() > 1) {
            for (int p = 0; p < event.getPointerCount(); p++) {
                drawingCurve.addPoint(event.getX(p), event.getY(p), event.getPointerId(p));
            }
        } else {
            drawingCurve.addPoint(event.getX(), event.getY(), 0);
        }
    }

    public void onTouchMove(MotionEvent event) {
        drawingCurve.resetDirtyRect(event.getX(), event.getY());

        if (event.getPointerCount() > 1) {
            for (int h = 0; h < event.getHistorySize(); h++) {
                for (int p = 0; p < event.getPointerCount(); p++) {
                    float historicalX = event.getHistoricalX(p, h);
                    float historicalY = event.getHistoricalY(p, h);
                    drawingCurve.expandDirtyRect(historicalX, historicalY);
                    drawingCurve.addPoint(event.getHistoricalX(p, h), event.getHistoricalY(p, h), event.getPointerId(p));
                }
            }
        } else {
            for (int i = 0; i < event.getHistorySize(); i++) {
                drawingCurve.addPoint(event.getHistoricalX(i), event.getHistoricalY(i), 0);
            }
            drawingCurve.addPoint(event.getX(), event.getY(), 0);
        }
    }

    public void onTouchUp(MotionEvent event) {
        drawingCurve.onTouchUp(event);
    }

    public boolean redo() {
        boolean surfaceChanged = drawingCurve.redo();
        if (surfaceChanged) {
            invalidate();
        }
        return surfaceChanged;
    }

    public boolean undo() {
        boolean surfaceChanged = drawingCurve.undo();
        if (surfaceChanged) {
            invalidate();
        }
        return surfaceChanged;
    }

    public void erase() {
        drawingCurve.erase();
    }

    public int getBrushColor() {
        return drawingCurve.getCurrentStrokeColor();
    }

    public Bitmap getDrawingBitmap() {
        return drawingCurve.getBitmap();
    }

    public float getBrushWidth() {
        return drawingCurve.getBrushWidth();
    }

    public ArrayList<Integer> getCurrentColors() {
        return drawingCurve.getCurrentColors();
    }
}
