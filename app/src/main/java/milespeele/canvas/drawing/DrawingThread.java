package milespeele.canvas.drawing;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.support.design.widget.Snackbar;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.inject.Inject;

import de.greenrobot.event.EventBus;
import milespeele.canvas.MainApp;
import milespeele.canvas.util.BitmapUtils;
import milespeele.canvas.util.EnumStore;
import milespeele.canvas.util.Logg;
import milespeele.canvas.view.ViewCanvasSurface;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by Miles Peele on 10/2/2015.
 */
public class DrawingThread extends Thread {

    private boolean mRun = false;

    private final SurfaceHolder mSurfaceHolder;
    private final Object mRunLock = new Object();
    private DrawingCurve drawingCurve;
    private Context mContext;

    public final static int RUNNING = 1;
    public final static int PAUSED = 2;
    private int mMode;

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
                        if (mRun && drawingCurve.isCanDraw())  {
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

    public void onTouchDown(MotionEvent event, float eventX, float eventY) {
        switch (drawingCurve.getState()) {
            case DRAW:
            case RAINBOW:
            case ERASE:
                drawingCurve.addPoint(eventX, eventY);
                break;
            case INK:
        }
    }

    public void onTouchMove(MotionEvent event, float eventX, float eventY) {
//        setInkPosition(event, eventX, eventY);
        switch (drawingCurve.getState()) {
            case RAINBOW:
            case ERASE:
            case DRAW:
                for (int i = 0; i < event.getHistorySize(); i++) {
                    drawingCurve.addPoint(event.getHistoricalX(i), event.getHistoricalY(i));
                }
                drawingCurve.addPoint(eventX, eventY);
                break;
            case INK:
                drawingCurve.setInkPaintColorBasedOnPixel(eventX, eventY);
                break;
        }
    }

    public void onTouchUp(MotionEvent event, float eventX, float eventY) {
        //        setInkPosition(event, eventX, eventY);
        drawingCurve.onTouchUp(eventX, eventY);
    }

    public void onSave() {
        drawingCurve.onSave();
    }
}
