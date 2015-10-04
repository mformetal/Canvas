package milespeele.canvas.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v4.view.MotionEventCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import milespeele.canvas.drawing.DrawingThread;
import milespeele.canvas.paint.PaintStyles;
import milespeele.canvas.util.BitmapUtils;
import milespeele.canvas.util.Logg;

/**
 * Created by Miles Peele on 10/2/2015.
 */
public class ViewCanvasSurface extends SurfaceView implements SurfaceHolder.Callback {

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
        setWillNotDraw(false);
        setSaveEnabled(true);
        getHolder().addCallback(this);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        thread = new DrawingThread(holder, getContext(), getWidth(), getHeight());
        thread.setRunning(true);
        thread.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        thread.setSurfaceSize(width, height);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        thread.onDestroy();
    }

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
        float eventX = event.getX();
        float eventY = event.getY();
        int actionMasked = MotionEventCompat.getActionMasked(event);

        switch (actionMasked & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_POINTER_DOWN:
                thread.onTouchDown(event, eventX, eventY);
                break;

            case MotionEvent.ACTION_MOVE:
                thread.onTouchMove(event, eventX, eventY);
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
                thread.onTouchUp(event, eventX, eventY);
                break;
        }
        return true;
    }

    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        if (!hasWindowFocus)  {
            thread.onSave();
        }
    }
}
