package milespeele.canvas.view;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.v4.view.MotionEventCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

import java.util.Random;

import javax.inject.Inject;

import de.greenrobot.event.EventBus;
import milespeele.canvas.MainApp;
import milespeele.canvas.R;
import milespeele.canvas.event.EventBrushSizeChosen;
import milespeele.canvas.event.EventColorChosen;
import milespeele.canvas.event.EventRedo;
import milespeele.canvas.event.EventUndo;
import milespeele.canvas.paint.PaintPath;
import milespeele.canvas.paint.PaintStack;
import milespeele.canvas.paint.PaintStyles;

/**
 * Created by milespeele on 7/2/15.
 */
public class ViewCanvas extends View {

    private static float STROKE_WIDTH = 5f;
    private boolean shouldErase = false;
    private boolean shouldRedraw = false;
    private boolean shouldInk = false;
    private int currentStrokeColor;
    private int currentBackgroundColor;
    private float lastTouchX, lastTouchY;
    private int width, height;

    private final RectF dirtyRect = new RectF();
    private PaintPath mPath;
    private PaintStack mPaths;
    private PaintStack redoPaths;
    private Paint curPaint;
    private Bitmap mBitmap;
    private Canvas mCanvas;
    private Matrix scaleMatrix;
    private ImageView eraser;
    private ImageView ink;

    @Inject EventBus bus;

    public ViewCanvas(Context context) {
        super(context);
        init();
    }

    public ViewCanvas(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public void init() {
        ((MainApp) getContext().getApplicationContext()).getApplicationComponent().inject(this);
        bus.register(this);

        Random rnd = new Random();
        currentStrokeColor = Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));
        currentBackgroundColor = Color.WHITE;

        curPaint = PaintStyles.randomStyle(currentStrokeColor, STROKE_WIDTH);

        mPath = new PaintPath(curPaint);
        mPaths = new PaintStack();
        redoPaths = new PaintStack();
        mPaths.push(mPath);

        scaleMatrix = new Matrix();

        setWillNotDraw(false);
        setSaveEnabled(true);
        setBackgroundColor(currentBackgroundColor);
        setLayerType(LAYER_TYPE_HARDWARE, null);
        setDrawingCacheQuality(DRAWING_CACHE_QUALITY_HIGH);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        width = w;
        height = h;

        scaleMatrix.reset();
        scaleMatrix.setScale(w, h);

        mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mBitmap);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (shouldRedraw) {
            for (PaintPath p: mPaths) {
                canvas.drawPath(p, p.getPaint());
            }
        } else {
            canvas.drawBitmap(mBitmap, 0, 0, null);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float eventX = event.getX();
        float eventY = event.getY();
        int actionMasked = MotionEventCompat.getActionMasked(event);

        switch (actionMasked & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_POINTER_DOWN:
                onTouchDown(event, eventX, eventY);
                break;

            case MotionEvent.ACTION_MOVE:
                onTouchMove(event, eventX, eventY);
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
                onTouchUp(event, eventX, eventY);
                break;
        }

        invalidate(Math.round(dirtyRect.left - STROKE_WIDTH / 2),
                Math.round(dirtyRect.top - STROKE_WIDTH / 2),
                Math.round(dirtyRect.right + STROKE_WIDTH / 2),
                Math.round(dirtyRect.bottom + STROKE_WIDTH / 2));

        lastTouchX = eventX;
        lastTouchY = eventY;
        return true;
    }

    private void onTouchDown(MotionEvent event, float eventX, float eventY) {
        if (!shouldInk) {
            lastTouchX = eventX;
            lastTouchY = eventY;
            mPath = new PaintPath(currentStyle());
            mPaths.push(mPath);
            mPath.moveTo(eventX, eventY);
        }

        setInkPosition(event, eventX, eventY);
        setEraserPosition(event, eventX, eventY);
    }

    private void onTouchMove(MotionEvent event, float eventX, float eventY) {
        resetDirtyRect(eventX, eventY);
        setInkPosition(event, eventX, eventY);
        setEraserPosition(event, eventX, eventY);

        if (!shouldInk) {
            for (int i = 0; i < event.getHistorySize(); i++) {
                float historicalX = event.getHistoricalX(i);
                float historicalY = event.getHistoricalY(i);
                expandDirtyRect(historicalX, historicalY);
                mPath.lineTo(historicalX, historicalY);
            }

            mPath.lineTo(eventX, eventY);
            mCanvas.drawPath(mPath, mPath.getPaint());
        }
    }

    private void onTouchUp(MotionEvent event, float eventX, float eventY) {
        setEraserPosition(event, eventX, eventY);
        setInkPosition(event, eventX, eventY);
    }

    private void expandDirtyRect(float historicalX, float historicalY) {
        if (historicalX < dirtyRect.left) {
            dirtyRect.left = historicalX;
        } else if (historicalX > dirtyRect.right) {
            dirtyRect.right = historicalX;
        }
        if (historicalY < dirtyRect.top) {
            dirtyRect.top = historicalY;
        } else if (historicalY > dirtyRect.bottom) {
            dirtyRect.bottom = historicalY;
        }
    }

    private void resetDirtyRect(float eventX, float eventY) {
        dirtyRect.left = Math.min(lastTouchX, eventX);
        dirtyRect.right = Math.max(lastTouchX, eventX);
        dirtyRect.top = Math.min(lastTouchY, eventY);
        dirtyRect.bottom = Math.max(lastTouchY, eventY);
    }

    private void setEraserPosition(MotionEvent event, float eventX, float eventY) {
        if (shouldErase && eraser != null) {
            eraser.setTranslationX(eventX);
            eraser.setTranslationY(eventY);

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    eraser.setVisibility(View.VISIBLE);
                    break;
                case MotionEvent.ACTION_UP:
                    eraser.setVisibility(View.GONE);
                    break;
            }
        }
    }

    private void setInkPosition(MotionEvent event, float eventX, float eventY) {
        if (shouldInk && ink != null) {
            if (eventsInRange(eventX, eventY)) {
                int color = mBitmap.getPixel((int) eventX, (int) eventY);
                ink.setBackgroundColor(color);

                ink.setTranslationX(eventX);
                ink.setTranslationY(eventY);

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        ink.setVisibility(View.VISIBLE);
                        break;
                    case MotionEvent.ACTION_UP:
                        ink.setVisibility(View.GONE);
                        shouldInk = false;
                        if (color != currentBackgroundColor)  {
                            curPaint.setColor((color == 0) ? currentStrokeColor : color);
                        } else {
                            curPaint.setColor(currentStrokeColor);
                        }
                        break;
                }
            }
        }
    }

    private boolean eventsInRange(float eventX, float eventY) {
        int x = Math.round(eventX), y = Math.round(eventY);
        return (x >= 0 && x <= mBitmap.getWidth() &&
                (y >= 0 && y <= mBitmap.getHeight()));
    }

    public void onEvent(EventColorChosen eventColorChosen) {
        if (eventColorChosen.color != 0) {
            if (eventColorChosen.which.equals(getResources().getString(R.string.TAG_FRAGMENT_FILL))) {
                fillCanvas(eventColorChosen.color);
            } else {
                changeColor(eventColorChosen.color);
            }
        }
    }

    public void onEvent(EventBrushSizeChosen eventBrushSizeChosen) {
        if (eventBrushSizeChosen.thickness != 0) {
            shouldInk = false;
            STROKE_WIDTH = eventBrushSizeChosen.thickness;
            curPaint.setStrokeWidth(eventBrushSizeChosen.thickness);
        }
    }

    public void onEvent(EventRedo eventRedo) {
        redo();
    }

    public void onEvent(EventUndo eventUndo) {
        undo();
    }

    public void changeColor(int color) {
        if (ink != null) {
            ink.setVisibility(View.GONE);
        }

        if (eraser != null) {
            eraser.setVisibility(View.GONE);
        }

        shouldInk = false;
        shouldErase = false;
        currentStrokeColor = color;
        curPaint.setColor(currentStrokeColor);
    }

    public void fillCanvas(int color) {
        shouldErase = false;
        for (PaintPath p: mPaths) {
            p.reset();
        }
        mPaths.clear();

        mPath = new PaintPath(currentStyle());
        mPaths.push(mPath);

        mBitmap.recycle();
        mBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mBitmap);

        ObjectAnimator.ofObject(this, "backgroundColor", new ArgbEvaluator(),
                currentBackgroundColor, color).setDuration(450).start();

        currentBackgroundColor = color;
        setDrawingCacheBackgroundColor(currentBackgroundColor);
        invalidate();
    }

    public float getBrushWidth() { return STROKE_WIDTH; }

    public Bitmap getBitmap() {
        return mBitmap;
    }

    public void undo() {
        if (!mPaths.isEmpty()) {
            PaintPath undo = mPaths.pop();
            redoPaths.push(undo);
            shouldRedraw = true;
            invalidate(Math.round(undo.getLeft() - STROKE_WIDTH),
                    Math.round(undo.getTop() - STROKE_WIDTH),
                    Math.round(undo.getRight() + STROKE_WIDTH),
                    Math.round(undo.getBottom() + STROKE_WIDTH));
        }
    }

    public void redo() {
        if (!redoPaths.isEmpty()) {
            PaintPath redo = redoPaths.pop();
            mPaths.push(redo);
            shouldRedraw = true;
            invalidate(Math.round(redo.getLeft() - STROKE_WIDTH),
                    Math.round(redo.getTop() - STROKE_WIDTH),
                    Math.round(redo.getRight() + STROKE_WIDTH),
                    Math.round(redo.getBottom() + STROKE_WIDTH));
        }
    }

    private Paint currentStyle() {
        if (shouldErase) {
            return (curPaint = PaintStyles.eraserPaint(currentBackgroundColor, STROKE_WIDTH * 4));
        } else {
            return new Paint(curPaint);
        }
    }
}