package milespeele.canvas.drawing;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.support.design.widget.Snackbar;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;

import javax.inject.Inject;

import de.greenrobot.event.EventBus;
import milespeele.canvas.MainApp;
import milespeele.canvas.util.EnumStore;
import milespeele.canvas.view.ViewCanvasSurface;

/**
 * Created by Miles Peele on 10/2/2015.
 */
public class DrawingThread extends Thread {

    private boolean mRun = false;
    private int mWidth, mHeight;

    private SurfaceHolder mSurfaceHolder;
    private final Object mRunLock = new Object();
    private DrawingCurve drawingCurve;
    private Context mContext;

    public DrawingThread(SurfaceHolder holder, Context context, int width, int height) {
        mSurfaceHolder = holder;
        mContext = context;
        drawingCurve = new DrawingCurve(context, width, height);

        mWidth = width;
        mHeight = height;
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
            mWidth = width;
            mHeight = height;
        }
    }

    @Override
    public void run() {
        while (mRun) {
            Canvas c = null;
            try {
                c = mSurfaceHolder.lockCanvas(null);
                synchronized (mSurfaceHolder) {
                    // Critical section. Do not allow mRun to be set false until
                    // we are sure all canvas draw operations are complete.
                    //
                    // If mRun has been toggled false, inhibit canvas operations.
                    synchronized (mRunLock) {
                        if (mRun) doDraw(c);
                    }
                }
            } finally {
                // do this in a finally so that if an exception is thrown
                // during the above, we don't leave the Surface in an
                // inconsistent state
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

//        setInkPosition(event, eventX, eventY);
//        setEraserPosition(event, eventX, eventY);
    }

    public void onTouchMove(MotionEvent event, float eventX, float eventY) {
//        setInkPosition(event, eventX, eventY);
//        setEraserPosition(event, eventX, eventY);

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
        }
    }

    public void onTouchUp(MotionEvent event, float eventX, float eventY) {
        //        setInkPosition(event, eventX, eventY);
//        setEraserPosition(event, eventX, eventY);

        drawingCurve.onTouchUp(eventX, eventY);
    }

    public void onSave() {
        drawingCurve.onSave();
    }
}
