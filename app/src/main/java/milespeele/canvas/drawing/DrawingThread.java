package milespeele.canvas.drawing;

import android.content.Context;
import android.graphics.Canvas;
import android.view.MotionEvent;
import android.view.SurfaceHolder;

import milespeele.canvas.util.FileUtils;
import milespeele.canvas.util.Logg;
import milespeele.canvas.view.ViewCanvasSurface;
import rx.android.schedulers.AndroidSchedulers;

public class DrawingThread extends Thread {

    private boolean mRun = false;

    private final SurfaceHolder mSurfaceHolder;
    private ViewCanvasSurface mSurface;
    private final Object mRunLock = new Object();
    private DrawingCurve drawingCurve;
    private Context mContext;

    public DrawingThread(ViewCanvasSurface surface, SurfaceHolder holder, Context context, int width, int height) {
        mSurface = surface;
        mSurfaceHolder = holder;
        mContext = context;
        drawingCurve = new DrawingCurve(context, width, height);
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
                            drawingCurve.drawBitmapToCanvas(c);
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
        setRunning(false);

        drawingCurve.onSave();

        FileUtils.compressBitmapAsObservable(drawingCurve.getBitmap())
                .subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(bytes -> {
                    FileUtils.cacheBitmap(mContext, bytes);

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
        if (event.getPointerCount() > 1) {
            for (int h = 0; h < event.getHistorySize(); h++) {
                for (int p = 0; p < event.getPointerCount(); p++) {
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

    public DrawingCurve getDrawingCurve() {
        return drawingCurve;
    }

}
