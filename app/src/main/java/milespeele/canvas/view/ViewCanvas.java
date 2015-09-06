package milespeele.canvas.view;

import android.animation.Animator;
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
import android.os.Parcelable;
import android.support.v4.view.MotionEventCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.FrameLayout;
import android.widget.ImageView;

import java.util.Random;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.greenrobot.event.EventBus;
import milespeele.canvas.MainApp;
import milespeele.canvas.R;
import milespeele.canvas.util.AbstractAnimatorListener;
import milespeele.canvas.util.BitmapUtils;
import milespeele.canvas.event.EventBrushChosen;
import milespeele.canvas.event.EventColorChosen;
import milespeele.canvas.event.EventShowColorize;
import milespeele.canvas.event.EventShowErase;
import milespeele.canvas.event.EventRedo;
import milespeele.canvas.event.EventUndo;
import milespeele.canvas.paint.PaintPath;
import milespeele.canvas.paint.PaintStack;
import milespeele.canvas.paint.PaintStyles;
import milespeele.canvas.util.Datastore;

public class ViewCanvas extends FrameLayout {

    public enum State {
        DRAW,
        INK,
        ERASE
    }

    @Bind(R.id.fragment_drawer_canvas_eraser) ImageView eraser;
    @Bind(R.id.fragment_drawer_eraser_colorizer) ImageView colorizer;

    private final static String CACHED_FILENAME = "cached";
    private static float STROKE_WIDTH = 5f;
    private int currentStrokeColor, currentBackgroundColor;
    private float lastTouchX, lastTouchY;
    private int width, height;
    private State state = State.DRAW;

    private final RectF dirtyRect = new RectF();
    private PaintPath mPath;
    private Paint curPaint;
    private Canvas mCanvas;
    private Matrix scaleMatrix;
    private PaintStack mPaths, redoPaths;
    private Bitmap drawingBitmap, cachedBitmap;

    @Inject EventBus bus;
    @Inject Datastore store;

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
        currentBackgroundColor = store.getLastBackgroundColor();

        curPaint = PaintStyles.normalPaint(currentStrokeColor, STROKE_WIDTH);

        mPath = new PaintPath(curPaint);
        mPaths = new PaintStack();
        redoPaths = new PaintStack();
        mPaths.push(mPath);

        scaleMatrix = new Matrix();

        setWillNotDraw(false);
        setSaveEnabled(true);
        setBackgroundColor(currentBackgroundColor);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        width = w;
        height = h;

        scaleMatrix.reset();
        scaleMatrix.setScale(w, h);

        drawingBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(drawingBitmap);

        cachedBitmap = BitmapUtils.getCachedBitmap(getContext(), CACHED_FILENAME);

        if (cachedBitmap != null) {
            mCanvas.drawBitmap(cachedBitmap, 0, 0, null);
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        ButterKnife.bind(this);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (cachedBitmap != null) {
            canvas.drawBitmap(cachedBitmap, 0, 0, null);
        }

        for (PaintPath p: mPaths) {
            canvas.drawPath(p, p.getPaint());
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
        if (state != State.INK) {
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

        if (state != State.INK) {
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
        if (state == State.ERASE) {
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
        if (state == State.INK && eventsInRange(eventX, eventY)) {
            int color = drawingBitmap.getPixel(Math.round(eventX), Math.round(eventY));
            colorizer.setBackgroundColor(color);

            colorizer.setTranslationX(eventX);
            colorizer.setTranslationY(eventY);

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    colorizer.setVisibility(View.VISIBLE);
                    break;
                case MotionEvent.ACTION_UP:
                    colorizer.setVisibility(View.GONE);
                    state = State.DRAW;
                    if (color != currentBackgroundColor)  {
                        curPaint.setColor((color == 0) ? currentStrokeColor : color);
                    } else {
                        curPaint.setColor(currentStrokeColor);
                    }
                    break;
            }
        }
    }

    private boolean eventsInRange(float eventX, float eventY) {
        int x = Math.round(eventX), y = Math.round(eventY);
        return (x >= 0 && x <= drawingBitmap.getWidth() &&
                (y >= 0 && y <= drawingBitmap.getHeight()));
    }

    public void onEvent(EventColorChosen eventColorChosen) {
        if (eventColorChosen.color != 0) {
            if (eventColorChosen.which.equals(getResources().getString(R.string.TAG_FRAGMENT_FILL))) {
                fillCanvas(eventColorChosen.color);
            } else {
                changeColor(eventColorChosen.color, eventColorChosen.opacity);
            }
        }
    }

    public void onEvent(EventBrushChosen eventBrushChosen) {
        state = State.DRAW;

        eraser.setVisibility(View.GONE);
        colorizer.setVisibility(View.GONE);

        STROKE_WIDTH = (eventBrushChosen.thickness != -1) ? eventBrushChosen.thickness : STROKE_WIDTH;

        curPaint.setStrokeWidth(STROKE_WIDTH);
    }

    public void onEvent(EventRedo eventRedo) {
        if (!redoPaths.isEmpty()) {
            PaintPath redo = redoPaths.pop();
            mPaths.push(redo);
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
            invalidate(Math.round(undo.getLeft() - STROKE_WIDTH),
                    Math.round(undo.getTop() - STROKE_WIDTH),
                    Math.round(undo.getRight() + STROKE_WIDTH),
                    Math.round(undo.getBottom() + STROKE_WIDTH));
        }
    }

    public void onEvent(EventShowErase eventErase) {
        if (eraser.getVisibility() == View.VISIBLE) {
            eraser.setVisibility(View.GONE);
            state = State.DRAW;
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
            state = State.ERASE;
        }
    }

    public void onEvent(EventShowColorize eventColorize) {
        eraser.setVisibility(View.GONE);
        colorizer.setX((float) getWidth() / 2);
        colorizer.setY((float) getHeight() / 2);
        colorizer.setBackgroundColor(drawingBitmap.getPixel(getWidth() / 2, getHeight() / 2));
        colorizer.setVisibility(View.VISIBLE);
        state = State.INK;
    }

    public void changeColor(int color, int opacity) {
        colorizer.setVisibility(View.GONE);
        eraser.setVisibility(View.GONE);

        state = State.DRAW;
        currentStrokeColor = color;
        curPaint.setAlpha(opacity);
        curPaint.setColor(currentStrokeColor);
    }

    public void fillCanvas(int color) {
        colorizer.setVisibility(View.GONE);
        eraser.setVisibility(View.GONE);

        state = State.DRAW;
        for (PaintPath p: mPaths) {
            p.reset();
        }
        mPaths.clear();

        mPath = new PaintPath(currentStyle());
        mPaths.push(mPath);

        if (cachedBitmap != null) {
            cachedBitmap.recycle();
            cachedBitmap = null;
        }
        drawingBitmap.recycle();
        drawingBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(drawingBitmap);

        ObjectAnimator background =
                ObjectAnimator.ofObject(this, "backgroundColor", new ArgbEvaluator(),
                currentBackgroundColor, color);
        background.setDuration(750);
        background.addListener(new AbstractAnimatorListener() {

            @Override
            public void onAnimationEnd(Animator animation) {
                mCanvas.drawColor(color);
            }
        });
        background.start();

        currentBackgroundColor = color;
        invalidate();
    }

    public float getBrushWidth() { return STROKE_WIDTH; }

    public Bitmap getDrawingBitmap() {
        return drawingBitmap;
    }

    public int getCurrentStrokeColor() { return currentStrokeColor; }

    private Paint currentStyle() {
        return (state == State.ERASE) ?
                PaintStyles.eraserPaint(currentBackgroundColor, eraser.getWidth()) :
                new Paint(curPaint);
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        BitmapUtils.cacheBitmap(getContext(), drawingBitmap, CACHED_FILENAME);
        store.setLastBackgroundColor(currentBackgroundColor);
        return super.onSaveInstanceState();
    }

}