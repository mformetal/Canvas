package milespeele.canvas.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Build;
import android.support.v4.view.MotionEventCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import java.util.ArrayList;

import milespeele.canvas.drawing.DrawingCurve;
import milespeele.canvas.util.Logg;

/**
 * Created by Miles Peele on 10/2/2015.
 */
public class ViewCanvasSurface extends SurfaceView
        implements SurfaceHolder.Callback, View.OnTouchListener, DrawingCurve.DrawingCurveListener {

    private DrawingCurve drawingCurve;
    private DrawingThread thread;

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
        drawingCurve.setListener(this);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        thread = new DrawingThread(holder);
        thread.setRunning(true);
        thread.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        thread.onDestroy();
        drawingCurve.onSave();
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        int actionMasked = MotionEventCompat.getActionMasked(event);

        switch (actionMasked & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_POINTER_DOWN:
                drawingCurve.onTouchDown(event);
                break;

            case MotionEvent.ACTION_MOVE:
                drawingCurve.onTouchMove(event);
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
                drawingCurve.onTouchUp(event);
                break;
        }

        invalidate(drawingCurve.getDirtyRect());

        return true;
    }

    public boolean redo() {
        boolean surfaceChanged = drawingCurve.redo();
        if (surfaceChanged) {
            invalidate(drawingCurve.getDirtyRect());
        }
        return surfaceChanged;
    }

    public boolean undo() {
        boolean surfaceChanged = drawingCurve.undo();
        if (surfaceChanged) {
            invalidate(drawingCurve.getDirtyRect());
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

    private class DrawingThread extends Thread {

        private boolean mRun = false;

        private final SurfaceHolder mSurfaceHolder;
        private final Object mRunLock = new Object();

        public DrawingThread(SurfaceHolder holder) {
            mSurfaceHolder = holder;
        }

        public void setRunning(boolean b) {
            synchronized (mRunLock) {
                mRun = b;
            }
        }

        @Override
        public void run() {
            while (mRun) {
                Canvas c = null;
                try {
                    c = mSurfaceHolder.lockCanvas(null);
                    synchronized (mSurfaceHolder) {
                        synchronized (mRunLock) {
                            if (mRun)  {
                                if (drawingCurve.iSafeToDraw()) {
                                    c.drawBitmap(drawingCurve.getBitmap(), 0, 0, null);
                                }
                            }
                        }
                    }
                } finally {
                    if (c != null) {
                        mSurfaceHolder.unlockCanvasAndPost(c);
                    }
                }
            }
        }

        public void onDestroy() {
            boolean retry = true;
            setRunning(false);
            while (retry) {
                try {
                    join();
                    retry = false;
                } catch (InterruptedException e) {
                    Logg.log(e);
                }
            }
        }

    }
}
