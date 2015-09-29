package milespeele.canvas.view;

import android.animation.Animator;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.GradientDrawable;
import android.os.Parcelable;
import android.support.annotation.NonNull;
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
import milespeele.canvas.drawing.DrawingCurve;
import milespeele.canvas.event.EventBrushChosen;
import milespeele.canvas.event.EventColorChosen;
import milespeele.canvas.event.EventRedo;
import milespeele.canvas.event.EventShowColorize;
import milespeele.canvas.event.EventShowErase;
import milespeele.canvas.event.EventUndo;
import milespeele.canvas.paint.PaintStyles;
import milespeele.canvas.util.AbstractAnimatorListener;
import milespeele.canvas.util.BitmapUtils;
import milespeele.canvas.util.Datastore;
import milespeele.canvas.util.Logg;

public class ViewCanvas extends FrameLayout {

    public enum State {
        DRAW,
        INK,
        ERASE,
        RAINBOW
    }

    @Bind(R.id.fragment_drawer_canvas_eraser) ImageView eraser;

    private int currentStrokeColor, currentBackgroundColor;

    private State state = State.DRAW;
    private Bitmap cachedBitmap;
    private DrawingCurve drawingCurve;

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

        setWillNotDraw(false);
        setSaveEnabled(true);
        setBackgroundColor(currentBackgroundColor);
        setLayerType(LAYER_TYPE_HARDWARE, null);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (drawingCurve != null) {
            drawingCurve.resize(w, h);
        } else {
            drawingCurve = new DrawingCurve(w, h);
        }

        cachedBitmap = BitmapUtils.getCachedBitmap(getContext());

        drawingCurve.setState(state);

        drawingCurve.drawBitmapToInternalCanvas(cachedBitmap);

        drawingCurve.setPaintColors(currentStrokeColor, currentBackgroundColor);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        ButterKnife.bind(this);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        drawingCurve.drawInkRect(canvas);
        drawingCurve.drawInternalBitmapToCanvas(canvas);
    }

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
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

        int[] rect = drawingCurve.getDirtyRectPos();
        invalidate(rect[0], rect[1], rect[2], rect[3]);
        return true;
    }

    private void onTouchDown(MotionEvent event, float eventX, float eventY) {
        switch (state) {
            case DRAW:
            case RAINBOW:
            case ERASE:
                drawingCurve.setState(state);
                drawingCurve.addPoint(eventX, eventY);
                break;
            case INK:
        }

        setInkPosition(event, eventX, eventY);
        setEraserPosition(event, eventX, eventY);
    }

    private void onTouchMove(MotionEvent event, float eventX, float eventY) {
        drawingCurve.resetRect(eventX, eventY);
        setInkPosition(event, eventX, eventY);
        setEraserPosition(event, eventX, eventY);

        switch (state) {
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

    private void onTouchUp(MotionEvent event, float eventX, float eventY) {
        setEraserPosition(event, eventX, eventY);
        setInkPosition(event, eventX, eventY);
        drawingCurve.onTouchUp(eventX, eventY);
    }

    private void setEraserPosition(MotionEvent event, float eventX, float eventY) {
        if (stateIsErase() && eventsInRange(eventX, eventY)) {
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
            int color = drawingCurve.getPixel(Math.round(eventX), Math.round(eventY));
            int colorToChangeTo;
            if (color != currentBackgroundColor)  {
                colorToChangeTo = (color == 0) ? currentStrokeColor : color;
            } else {
                colorToChangeTo = currentStrokeColor;
            }

            drawingCurve.setInkPaintColor(colorToChangeTo);

            switch (event.getAction()) {
                case MotionEvent.ACTION_UP:
                    state = State.DRAW;
                    drawingCurve.setState(state);
                    if (color != currentBackgroundColor)  {
                        drawingCurve.setPaintColor((color == 0) ? currentStrokeColor : color);
                    } else {
                        drawingCurve.setPaintColor(currentStrokeColor);
                    }
                    break;
            }
        }
    }

    private boolean eventsInRange(float eventX, float eventY) {
        int x = Math.round(eventX), y = Math.round(eventY);
        return (x >= 0 && x <= drawingCurve.getWidth() &&
                (y >= 0 && y <= drawingCurve.getHeight()));
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

        drawingCurve.setStaticStrokeWidth(eventBrushChosen.thickness);

        if (eventBrushChosen.paint != null) {
            if (eventBrushChosen.paint.getShader() != null) {
                state = State.RAINBOW;
                eventBrushChosen.paint.setShader(null);
            }
            drawingCurve.setPaint(eventBrushChosen.paint);
            drawingCurve.setPaintColor(currentStrokeColor);
        }

        drawingCurve.setPaintThickness(eventBrushChosen.thickness);
    }

    public void onEvent(EventRedo eventRedo) {
        if (drawingCurve.redo(cachedBitmap)) {
            int[] rect = drawingCurve.getDirtyRectPos();
            invalidate(rect[0], rect[1], rect[2], rect[3]);
        }
    }

    public void onEvent(EventUndo eventUndo) {
        if (drawingCurve.undo(cachedBitmap)) {
            int[] rect = drawingCurve.getDirtyRectPos();
            invalidate(rect[0], rect[1], rect[2], rect[3]);
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

        drawingCurve.setState(state);
    }

    public void onEvent(EventShowColorize eventColorize) {
        if (!stateIsInk()) {
            eraser.setVisibility(View.GONE);
            drawingCurve.setInkPaintColor(currentStrokeColor);
            state = State.INK;
        } else {
            state = State.DRAW;
        }

        drawingCurve.setState(state);

        int[] rect = drawingCurve.getInkRectPos();
        invalidate(rect[0], rect[1], rect[2], rect[3]);
    }

    public void changeColor(int color, int opacity) {
        eraser.setVisibility(View.GONE);

        state = State.DRAW;
        currentStrokeColor = color;
        drawingCurve.setPaintAlpha(opacity);
        drawingCurve.setPaintColor(currentStrokeColor);
        drawingCurve.setState(state);
    }

    public void fillCanvas(int color) {
        eraser.setVisibility(View.GONE);

        state = State.DRAW;

        if (cachedBitmap != null) {
            cachedBitmap.recycle();
            cachedBitmap = null;
        }

        drawingCurve.hardReset();

        ObjectAnimator background =
                ObjectAnimator.ofObject(this, "backgroundColor", new ArgbEvaluator(),
                        currentBackgroundColor, color);
        background.setDuration(1000);
        background.addListener(new AbstractAnimatorListener() {

            @Override
            public void onAnimationEnd(Animator animation) {
                drawingCurve.drawColorToInternalCanvas(color);
            }
        });
        background.start();

        currentBackgroundColor = color;
        invalidate();
    }

    public float getBrushWidth() { return drawingCurve.getStaticStrokeWidth(); }

    public Bitmap getDrawingBitmap() {
        return drawingCurve.getBitmap();
    }

    public int getCurrentStrokeColor() { return currentStrokeColor; }

    private boolean stateIsInk() {
        return state == State.INK;
    }

    private boolean stateIsErase() { return state == State.ERASE; }

    @Override
    protected Parcelable onSaveInstanceState() {
        BitmapUtils.observableCacheBitmap(getContext(), getDrawingBitmap());
        store.setLastBackgroundColor(currentBackgroundColor);
        return super.onSaveInstanceState();
    }

}