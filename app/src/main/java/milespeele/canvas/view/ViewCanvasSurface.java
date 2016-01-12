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

import milespeele.canvas.R;
import milespeele.canvas.drawing.DrawingCurve;
import milespeele.canvas.util.Logg;

/**
 * Created by Miles Peele on 10/2/2015.
 */
public class ViewCanvasSurface extends SurfaceView implements SurfaceHolder.Callback {

    private DrawingCurve mDrawingCurve;
    private DrawingThread mDrawingThread;

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
        mDrawingCurve = new DrawingCurve(getContext());

        setLayerType(LAYER_TYPE_NONE, null);

        setWillNotDraw(false);
        setSaveEnabled(true);

        getHolder().addCallback(this);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        holder.setFixedSize(getWidth(), getHeight());

        mDrawingThread = new DrawingThread(holder);
        mDrawingThread.setRunning(true);
        mDrawingThread.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        mDrawingThread.onDestroy();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            float y = event.getY();
            if (y <= getResources().getDimension(R.dimen.status_bar_height)) {
                return false;
            }

            if (y >= getHeight() - getResources().getDimension(R.dimen.status_bar_height)) {
                return false;
            }
        }

        return mDrawingCurve.onTouchEvent(event);
    }

    public void setListener(DrawingCurve.DrawingCurveListener listener) {
        mDrawingCurve.setListener(listener);
    }

    public void onOptionsMenuAccept() { mDrawingCurve.onOptionsMenuAccept(); }

    public void onOptionsMenuCancel() { mDrawingCurve.onOptionsMenuCancel(); }

    public void ink() {
        mDrawingCurve.ink();
    }

    public boolean redo() {
        return mDrawingCurve.redo();
    }

    public boolean undo() {
        return mDrawingCurve.undo();
    }

    public void erase() {
        mDrawingCurve.erase();
    }

    public int getBrushColor() {
        return mDrawingCurve.getStrokeColor();
    }

    public int getBackgroundColor() { return mDrawingCurve.getBackgroundColor(); }

    public Bitmap getDrawingBitmap() {
        return mDrawingCurve.getBitmap();
    }

    public Paint getCurrentPaint() { return mDrawingCurve.getPaint(); }

    private class DrawingThread extends Thread {

        private boolean mRun = false;

        private final SurfaceHolder mSurfaceHolder;
        private final Object mRunLock = new Object();

        public DrawingThread(SurfaceHolder holder) {
            super("drawingThread");
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
                                mDrawingCurve.drawToSurfaceView(c);
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