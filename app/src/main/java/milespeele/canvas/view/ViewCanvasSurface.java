package milespeele.canvas.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Build;
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

        if (oldh == 0 || oldw == 0) {
            drawingCurve = new DrawingCurve(getContext(), w, h);
            drawingCurve.setListener(this);
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        holder.setFixedSize(getWidth(), getHeight());
        holder.setKeepScreenOn(true);

        thread = new DrawingThread(holder);
        thread.setRunning(true);
        thread.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        drawingCurve.onSave();
        thread.onDestroy();
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return drawingCurve.onTouchEvent(event);
    }

    @Override
    public void showButton(String text) {
        ((ViewCanvasLayout) getParent()).setButtonVisible(text);
    }

    @Override
    public void hideButton() {
        ((ViewCanvasLayout) getParent()).setButtonGone();
    }

    public void onButtonClicked() { drawingCurve.onButtonClicked(); }

    public boolean ink() {
        return drawingCurve.ink();
    }

    public boolean redo() {
        return drawingCurve.redo();
    }

    public boolean undo() {
        return drawingCurve.undo();
    }

    public void erase() {
        drawingCurve.erase();
    }

    public int getBrushColor() {
        return drawingCurve.getStrokeColor();
    }

    public Bitmap getDrawingBitmap() {
        return drawingCurve.getBitmap();
    }

    public Paint getCurrentPaint() { return drawingCurve.getPaint(); }

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
                    if (mSurfaceHolder.getSurface().isValid()) {
                        c = mSurfaceHolder.lockCanvas();
                    }

                    synchronized (mSurfaceHolder) {
                        synchronized (mRunLock) {
                            if (mRun)  {
                                drawingCurve.drawToSurfaceView(c);
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
