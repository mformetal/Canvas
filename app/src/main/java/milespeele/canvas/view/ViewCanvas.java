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
import android.widget.FrameLayout;
import android.widget.ImageView;

import java.util.Random;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.greenrobot.event.EventBus;
import milespeele.canvas.MainApp;
import milespeele.canvas.R;
import milespeele.canvas.service.ServiceBitmapUtils;
import milespeele.canvas.util.AbstractAnimatorListener;
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
import milespeele.canvas.util.Point;

public class ViewCanvas extends FrameLayout {

    public enum State {
        DRAW,
        INK,
        ERASE
    }

    @Bind(R.id.fragment_drawer_canvas_eraser) ImageView eraser;

    private float lastWidth;
    private float lastVelocity;
    private static final float VELOCITY_FILTER_WEIGHT = 0.2f;
    private static float STROKE_WIDTH = 5f;
    private int currentStrokeColor, currentBackgroundColor;
    private float lastTouchX, lastTouchY;
    private int width, height;

    private State state = State.DRAW;
    private final RectF dirtyRect = new RectF();
    private final RectF inkRect = new RectF();
    private PaintPath mPath;
    private Paint curPaint;
    private Paint inkPaint;
    private Canvas mCanvas;
    private Matrix scaleMatrix;
    private PaintStack mPaths, redoPaths;
    private Bitmap drawingBitmap, cachedBitmap;
    private Point previousPoint, startPoint, currentPoint;

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

        curPaint = PaintStyles.normal(currentStrokeColor, STROKE_WIDTH);
        inkPaint = PaintStyles.normal(currentStrokeColor, STROKE_WIDTH);
        inkPaint.setStyle(Paint.Style.FILL_AND_STROKE);

        mPath = new PaintPath(curPaint);
        mPaths = new PaintStack();
        redoPaths = new PaintStack();
        mPaths.push(mPath);

        scaleMatrix = new Matrix();

        setWillNotDraw(false);
        setSaveEnabled(true);
        setBackgroundColor(currentBackgroundColor);
        setLayerType(LAYER_TYPE_SOFTWARE, null);
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

        cachedBitmap = ServiceBitmapUtils.getCachedBitmap(getContext());

        if (cachedBitmap != null) {
            mCanvas.drawBitmap(cachedBitmap, 0, 0, null);
        }

        inkRect.left = mCanvas.getWidth() / 40;
        inkRect.top = mCanvas.getWidth() / 40;
        inkRect.right = mCanvas.getWidth() / 6;
        inkRect.bottom = mCanvas.getWidth() / 6;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        ButterKnife.bind(this);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        drawCachedBitmap(canvas);

        canvas.drawBitmap(drawingBitmap, 0, 0, null);

        if (stateIsInk()) {
            canvas.drawRect(inkRect, inkPaint);
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
        if (!stateIsInk()) {
            lastTouchX = eventX;
            lastTouchY = eventY;

            currentPoint = new Point(event.getX(), event.getY(), System.currentTimeMillis());
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

        if (!stateIsInk()) {
            for (int i = 0; i < event.getHistorySize(); i++) {
                float historicalX = event.getHistoricalX(i);
                float historicalY = event.getHistoricalY(i);
                expandDirtyRect(historicalX, historicalY);

                startPoint = previousPoint;
                previousPoint = currentPoint;
                currentPoint = new Point(eventX, eventY, System.currentTimeMillis());

                float velocity = currentPoint.velocityFrom(previousPoint);
                velocity = VELOCITY_FILTER_WEIGHT * velocity + (1 - VELOCITY_FILTER_WEIGHT) * lastVelocity;
                float strokeWidth = STROKE_WIDTH - velocity;

                lastVelocity = velocity;
                lastWidth = strokeWidth;
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
        currentPoint = new Point(eventX, eventY, System.currentTimeMillis());;
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
        if (stateIsInk() && eventsInRange(eventX, eventY)) {
            int color = drawingBitmap.getPixel(Math.round(eventX), Math.round(eventY));
            int colorToChangeTo;
            if (color != currentBackgroundColor)  {
               colorToChangeTo = (color == 0) ? currentStrokeColor : color;
            } else {
                colorToChangeTo = currentStrokeColor;
            }

            inkPaint.setColor(colorToChangeTo);

            switch (event.getAction()) {
                case MotionEvent.ACTION_UP:
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

        STROKE_WIDTH = eventBrushChosen.thickness;

        if (eventBrushChosen.paint != null) {
            curPaint.set(eventBrushChosen.paint);
            curPaint.setColor(currentStrokeColor);
        }

        curPaint.setStrokeWidth(STROKE_WIDTH);
    }

    public void onEvent(EventRedo eventRedo) {
        if (!redoPaths.isEmpty()) {
            PaintPath redo = redoPaths.pop();
            mPaths.push(redo);

            redrawToOffscreenBitmap();

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

            redrawToOffscreenBitmap();

            invalidate(Math.round(undo.getLeft() - STROKE_WIDTH),
                    Math.round(undo.getTop() - STROKE_WIDTH),
                    Math.round(undo.getRight() + STROKE_WIDTH),
                    Math.round(undo.getBottom() + STROKE_WIDTH));
        }
    }

    private void redrawToOffscreenBitmap() {
        drawingBitmap.recycle();
        drawingBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(drawingBitmap);

        for (PaintPath path: mPaths) {
            mCanvas.drawPath(path, path.getPaint());
        }

        drawCachedBitmap(mCanvas);
    }

    private void drawCachedBitmap(Canvas canvas) {
        if (cachedBitmap != null) {
            canvas.drawBitmap(cachedBitmap, 0, 0, null);
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
        if (!stateIsInk()) {
            eraser.setVisibility(View.GONE);
            inkPaint.setColor(currentStrokeColor);
            state = State.INK;
        } else {
            state = State.DRAW;
        }
        invalidate(Math.round(inkRect.left),
                Math.round(inkRect.top),
                Math.round(inkRect.right),
                Math.round(inkRect.bottom));
    }

    public void changeColor(int color, int opacity) {
        eraser.setVisibility(View.GONE);

        state = State.DRAW;
        currentStrokeColor = color;
        curPaint.setAlpha(opacity);
        curPaint.setColor(currentStrokeColor);
    }

    public void fillCanvas(int color) {
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
                PaintStyles.erase(currentBackgroundColor, eraser.getWidth()) :
                new Paint(curPaint);
    }

    private boolean stateIsInk() {
        return state == State.INK;
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        getContext().startService(ServiceBitmapUtils.newIntent(
                getContext(),
                ServiceBitmapUtils.compressBitmapAsByteArray(drawingBitmap)));
        store.setLastBackgroundColor(currentBackgroundColor);
        return super.onSaveInstanceState();
    }

}