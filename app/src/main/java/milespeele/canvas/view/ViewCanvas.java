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
import android.graphics.drawable.GradientDrawable;
import android.support.v4.view.MotionEventCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import java.util.Random;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.greenrobot.event.EventBus;
import milespeele.canvas.MainApp;
import milespeele.canvas.R;
import milespeele.canvas.util.Logg;
import milespeele.canvas.util.PathPoint;
import milespeele.canvas.event.EventBrushChosen;
import milespeele.canvas.event.EventColorChosen;
import milespeele.canvas.event.EventShowColorize;
import milespeele.canvas.event.EventShowErase;
import milespeele.canvas.event.EventRedo;
import milespeele.canvas.event.EventUndo;
import milespeele.canvas.paint.PaintPath;
import milespeele.canvas.paint.PaintStack;
import milespeele.canvas.paint.PaintStyles;

/**
 * Created by milespeele on 7/2/15.
 */
public class ViewCanvas extends FrameLayout {

    @Bind(R.id.fragment_drawer_canvas_eraser) ImageView eraser;
    @Bind(R.id.fragment_drawer_eraser_colorizer) ImageView colorizer;

    private float lastWidth;
    private float lastVelocity;
    private static final float VELOCITY_FILTER_WEIGHT = 0.2f;
    private static float STROKE_WIDTH = 5f;
    private static int ALPHA = 255;
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

    private PathPoint previousPoint;
    private PathPoint startPoint;
    private PathPoint currentPoint;

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
    protected void onFinishInflate() {
        super.onFinishInflate();
        ButterKnife.bind(this);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        //super.onDraw(canvas);
        if (shouldRedraw) {
            for (PaintPath p: mPaths) {
                mCanvas.drawPath(p, p.getPaint());
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
            currentPoint = new PathPoint(event.getX(), event.getY(), System.currentTimeMillis());
            previousPoint = currentPoint;
            startPoint = previousPoint;

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
                startPoint = previousPoint;
                previousPoint = currentPoint;
                currentPoint = new PathPoint(event.getX(), event.getY(), System.currentTimeMillis());
                float velocity = VELOCITY_FILTER_WEIGHT * currentPoint.velocityFrom(previousPoint) +
                        (1 - VELOCITY_FILTER_WEIGHT) * lastVelocity;
                curPaint.setStrokeWidth(STROKE_WIDTH - velocity);
                mPath.lineTo(historicalX, historicalY);
            }

            mPath.lineTo(eventX, eventY);
            mCanvas.drawPath(mPath, mPath.getPaint());
        }
    }

    private void onTouchUp(MotionEvent event, float eventX, float eventY) {
        setEraserPosition(event, eventX, eventY);
        setInkPosition(event, eventX, eventY);

        startPoint = previousPoint;
        previousPoint = currentPoint;
        currentPoint = new PathPoint(event.getX(), event.getY(), System.currentTimeMillis());
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
        if (shouldErase) {
            eraser.setTranslationX(eventX);
            eraser.setTranslationY(eventY);

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    eraser.setVisibility(View.VISIBLE);
                    break;
            }
        }
    }

    private void setInkPosition(MotionEvent event, float eventX, float eventY) {
        if (shouldInk) {
            if (eventsInRange(eventX, eventY)) {
                int color = mBitmap.getPixel((int) eventX, (int) eventY);
                colorizer.setBackgroundColor(color);

                colorizer.setTranslationX(eventX);
                colorizer.setTranslationY(eventY);

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        colorizer.setVisibility(View.VISIBLE);
                        break;
                    case MotionEvent.ACTION_UP:
                        colorizer.setVisibility(View.GONE);
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

    public void onEvent(EventBrushChosen eventBrushChosen) {
        shouldErase = false;
        shouldInk = false;

        eraser.setVisibility(View.GONE);
        colorizer.setVisibility(View.GONE);

        STROKE_WIDTH = (eventBrushChosen.thickness != -1) ? eventBrushChosen.thickness : STROKE_WIDTH;
        ALPHA = (eventBrushChosen.alpha != -1) ? eventBrushChosen.alpha : ALPHA;

        curPaint.setAlpha(ALPHA);
        curPaint.setStrokeWidth(STROKE_WIDTH);
    }

    public void onEvent(EventRedo eventRedo) {
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

    public void onEvent(EventUndo eventUndo) {
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

    public void onEvent(EventShowErase eventErase) {
        if (eraser.getVisibility() == View.VISIBLE) {
            eraser.setVisibility(View.GONE);
            shouldErase = false;
            shouldInk = false;
        } else {
            double darkness = 1 - (0.299 * Color.red(currentBackgroundColor) +
                    0.587 * Color.green(currentBackgroundColor) +
                    0.114 * Color.blue(currentBackgroundColor)) / 255;
            if (darkness < 0.5) {
                ((GradientDrawable) eraser.getDrawable()).setColor(Color.BLACK);
            } else {
                ((GradientDrawable) eraser.getDrawable()).setColor(Color.WHITE);
            }
            eraser.setVisibility(View.VISIBLE);
            eraser.setX((float) getWidth() / 2);
            eraser.setY((float) getHeight() / 2);
            shouldErase = true;
            shouldInk = false;
        }
    }

    public void onEvent(EventShowColorize eventColorize) {
        eraser.setVisibility(View.GONE);
        colorizer.setX((float) getWidth() / 2);
        colorizer.setY((float) getHeight() / 2);
        colorizer.setBackgroundColor(mBitmap.getPixel(getWidth() / 2, getHeight() / 2));
        colorizer.setVisibility(View.VISIBLE);
        shouldInk = true;
        shouldErase = false;
    }

    public void changeColor(int color) {
        colorizer.setVisibility(View.GONE);
        eraser.setVisibility(View.GONE);

        shouldInk = false;
        shouldErase = false;
        currentStrokeColor = color;
        curPaint.setColor(currentStrokeColor);
    }

    public void fillCanvas(int color) {
        colorizer.setVisibility(View.GONE);
        eraser.setVisibility(View.GONE);

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
                currentBackgroundColor, color).setDuration(750).start();

        currentBackgroundColor = color;
        setDrawingCacheBackgroundColor(currentBackgroundColor);
        invalidate();
    }

    public float getBrushWidth() { return STROKE_WIDTH; }

    public int getPaintAlpha() { return ALPHA; }

    public Bitmap getBitmap() {
        return mBitmap;
    }

    private Paint currentStyle() {
        if (shouldErase) {
            return PaintStyles.eraserPaint(currentBackgroundColor, eraser.getWidth());
        } else {
            return new Paint(curPaint);
        }
    }
}