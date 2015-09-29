package milespeele.canvas.view;

import android.animation.Animator;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.view.MotionEventCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

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
import milespeele.canvas.util.AbstractAnimatorListener;
import milespeele.canvas.util.BitmapUtils;
import milespeele.canvas.util.EnumStore;

public class ViewCanvas extends FrameLayout {

    public enum State {
        DRAW,
        INK,
        ERASE,
        RAINBOW
    }

    @Bind(R.id.fragment_drawer_canvas_eraser) ImageView eraser;

    private EnumStore enumStore;
    private DrawingCurve drawingCurve;

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

        enumStore = new EnumStore();

        setWillNotDraw(false);
        setSaveEnabled(true);
        setLayerType(LAYER_TYPE_HARDWARE, null);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (drawingCurve != null) {
            drawingCurve.resize(w, h);
        } else {
            drawingCurve = new DrawingCurve(getContext(), w, h);
        }

        enumStore.setListener(drawingCurve);
        enumStore.setValue(State.DRAW);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        ButterKnife.bind(this);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        drawingCurve.drawToViewCanvas(canvas);
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
        switch (enumStore.getValue()) {
            case DRAW:
            case RAINBOW:
            case ERASE:
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

        switch (enumStore.getValue()) {
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
            drawingCurve.setInkPaintColorBasedOnPixel(eventX, eventY);

            switch (event.getAction()) {
                case MotionEvent.ACTION_UP:
                    enumStore.setValue(State.DRAW);
                    drawingCurve.onInkPaintTouchUp();
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
        enumStore.setValue(State.DRAW);

        eraser.setVisibility(View.GONE);

        drawingCurve.setStaticStrokeWidth(eventBrushChosen.thickness);

        if (eventBrushChosen.paint != null) {
            if (eventBrushChosen.paint.getShader() != null) {
                enumStore.setValue(State.RAINBOW);
                eventBrushChosen.paint.setShader(null);
            }
            drawingCurve.setPaint(eventBrushChosen.paint);
        }

        drawingCurve.setPaintThickness(eventBrushChosen.thickness);
    }

    public void onEvent(EventRedo eventRedo) {
        if (drawingCurve.redo()) {
            int[] rect = drawingCurve.getDirtyRectPos();
            invalidate(rect[0], rect[1], rect[2], rect[3]);
            return;
        }
        Snackbar.make(this, "No more redo!", Snackbar.LENGTH_SHORT).show();
    }

    public void onEvent(EventUndo eventUndo) {
        if (drawingCurve.undo()) {
            int[] rect = drawingCurve.getDirtyRectPos();
            invalidate(rect[0], rect[1], rect[2], rect[3]);
            return;
        }
        Snackbar.make(this, "No more undo!", Snackbar.LENGTH_SHORT).show();
    }

    public void onEvent(EventShowErase eventErase) {
        if (eraser.getVisibility() == View.VISIBLE) {
            eraser.setVisibility(View.GONE);
            enumStore.setValue(State.DRAW);
        } else {
            int background = drawingCurve.getCurrentBackgroundColor();
            double darkness = 1 - (0.299 * Color.red(background) +
                    0.587 * Color.green(background) +
                    0.114 * Color.blue(background)) / 255;
            if (darkness < 0.5) {
                ((GradientDrawable) eraser.getDrawable()).setColor(Color.BLACK);
            } else {
                ((GradientDrawable) eraser.getDrawable()).setColor(Color.WHITE);
            }

            eraser.setVisibility(View.VISIBLE);
            eraser.setX((float) getWidth() / 2);
            eraser.setY((float) getHeight() / 2);
            enumStore.setValue(State.ERASE);
        }
    }

    public void onEvent(EventShowColorize eventColorize) {
        if (!stateIsInk()) {
            eraser.setVisibility(View.GONE);
            enumStore.setValue(State.INK);
        } else {
            enumStore.setValue(State.DRAW);
        }

        int[] rect = drawingCurve.getInkRectPos();
        invalidate(rect[0], rect[1], rect[2], rect[3]);
    }

    public void changeColor(int color, int opacity) {
        eraser.setVisibility(View.GONE);

        enumStore.setValue(State.DRAW);
        drawingCurve.setPaintAlpha(opacity);
        drawingCurve.setPaintColor(color);
    }

    public void fillCanvas(int color) {
        eraser.setVisibility(View.GONE);

        enumStore.setValue(State.DRAW);

        drawingCurve.hardReset(color);

        ObjectAnimator background =
                ObjectAnimator.ofObject(this, "backgroundColor", new ArgbEvaluator(),
                        drawingCurve.getCurrentBackgroundColor(), color);
        background.setDuration(1000);
        background.addListener(new AbstractAnimatorListener() {

            @Override
            public void onAnimationEnd(Animator animation) {
                drawingCurve.drawColorToInternalCanvas(color);
            }
        });
        background.start();

        invalidate();
    }

    public float getBrushWidth() { return drawingCurve.getStaticStrokeWidth(); }

    public Bitmap getDrawingBitmap() {
        return drawingCurve.getBitmap();
    }

    public int getCurrentStrokeColor() { return drawingCurve.getPaintColor(); }

    private boolean stateIsInk() {
        return enumStore.getValue() == State.INK;
    }

    private boolean stateIsErase() { return enumStore.getValue() == State.ERASE; }

    @Override
    protected Parcelable onSaveInstanceState() {
        drawingCurve.onSave();
        return super.onSaveInstanceState();
    }

}