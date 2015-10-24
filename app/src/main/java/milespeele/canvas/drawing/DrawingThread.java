package milespeele.canvas.drawing;

import android.content.Context;
import android.graphics.Canvas;
import android.view.MotionEvent;
import android.view.SurfaceHolder;

import milespeele.canvas.util.BitmapUtils;
import milespeele.canvas.util.Logg;
import rx.android.schedulers.AndroidSchedulers;

/**
 * Created by Miles Peele on 10/2/2015.
 */
public class DrawingThread extends Thread {

    private boolean mRun = false;

    private final SurfaceHolder mSurfaceHolder;
    private final Object mRunLock = new Object();
    private DrawingCurve drawingCurve;
    private Context mContext;

    public DrawingThread(SurfaceHolder holder, Context context, int width, int height) {
        mSurfaceHolder = holder;
        mContext = context;
        drawingCurve = new DrawingCurve(context, width, height);
    }

    public void onDestroy() {
        setRunning(false);

        BitmapUtils.compressBitmapAsObservable(drawingCurve.getBitmap())
                .subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(bytes -> {
                    BitmapUtils.cache(mContext, bytes);

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
                });
    }

    public void setRunning(boolean b) {
        // Do not allow mRun to be modified while any canvas operations
        // are potentially in-flight. See doDraw().
        synchronized (mRunLock) {
            mRun = b;
        }
    }

    public void setSurfaceSize(int width, int height) {
        // synchronized to make sure these all change atomically
        synchronized (mSurfaceHolder) {
            drawingCurve.resize(width, height);
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
                        if (mRun && drawingCurve.canDraw())  {
                            doDraw(c);
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

    private void doDraw(Canvas canvas) {
        drawingCurve.drawToViewCanvas(canvas);
    }

    public void onTouchDown(MotionEvent event) {
        drawingCurve.addPoint(event.getX(), event.getY());
    }

    public void onTouchMove(MotionEvent event) {
        drawingCurve.parseMotionEvent(event);
    }

    public void onTouchUp(MotionEvent event) {
        drawingCurve.onTouchUp(event.getX(), event.getY());
    }

    public void onSave() {
        drawingCurve.onSave();
    }

    public DrawingCurve getDrawingCurve() {
        return drawingCurve;
    }
}
