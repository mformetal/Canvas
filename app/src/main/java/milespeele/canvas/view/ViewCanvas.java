package milespeele.canvas.view;

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
import milespeele.canvas.drawing.DrawingCurve;
import milespeele.canvas.service.ServiceBitmapUtils;
import milespeele.canvas.event.EventBrushChosen;
import milespeele.canvas.event.EventColorChosen;
import milespeele.canvas.event.EventShowColorize;
import milespeele.canvas.event.EventShowErase;
import milespeele.canvas.event.EventRedo;
import milespeele.canvas.event.EventUndo;
import milespeele.canvas.paint.PaintStyles;
import milespeele.canvas.util.Datastore;
import milespeele.canvas.util.Logg;

public class ViewCanvas extends FrameLayout {

    public enum State {
        DRAW,
        INK,
        ERASE
    }

    @Bind(R.id.fragment_drawer_canvas_eraser) ImageView eraser;

    private int currentStrokeColor, currentBackgroundColor;
    private int width, height;
    private float lastTouchX, lastTouchY;

    private State state = State.DRAW;
    private final RectF inkRect = new RectF();
    private Paint curPaint;
    private Paint inkPaint;
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
        setLayerType(LAYER_TYPE_SOFTWARE, null);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        width = w;
        height = h;

        drawingCurve = new DrawingCurve(w, h);

        curPaint = PaintStyles.normal(currentStrokeColor, drawingCurve.getStaticStrokeWidth());
        inkPaint = PaintStyles.normal(currentStrokeColor, drawingCurve.getStaticStrokeWidth());
        inkPaint.setStyle(Paint.Style.FILL_AND_STROKE);

        cachedBitmap = ServiceBitmapUtils.getCachedBitmap(getContext());

        drawingCurve.drawBitmapToInternalCanvas(cachedBitmap);

        inkRect.left = w / 40;
        inkRect.top = w / 40;
        inkRect.right = w / 5;
        inkRect.bottom = w / 5;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        ButterKnife.bind(this);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        drawingCurve.drawBitmapToInternalCanvas(cachedBitmap);
        drawingCurve.drawInternalBitmapToCanvas(canvas);

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

        invalidate(drawingCurve.getDirtyRect());

        lastTouchX = eventX;
        lastTouchY = eventY;
        return true;
    }

    private void onTouchDown(MotionEvent event, float eventX, float eventY) {
        if (!stateIsInk()) {
            lastTouchX = eventX;
            lastTouchY = eventY;

            drawingCurve.setPaint(currentStyle());
            drawingCurve.addPoint(eventX, eventY, event.getDownTime());
        }

        setInkPosition(event, eventX, eventY);
        setEraserPosition(event, eventX, eventY);
    }

    private void onTouchMove(MotionEvent event, float eventX, float eventY) {
        drawingCurve.resetRect(eventX, eventY);
        setInkPosition(event, eventX, eventY);
        setEraserPosition(event, eventX, eventY);

        if (!stateIsInk()) {
            for (int i = 0; i < event.getHistorySize(); i++) {
                float historicalX = event.getHistoricalX(i);
                float historicalY = event.getHistoricalY(i);
                long historicalEventTime = event.getHistoricalEventTime(i);
                drawingCurve.updateRect(historicalX, historicalY);

                drawingCurve.addPoint(historicalX, historicalY, historicalEventTime);
            }

            drawingCurve.addPoint(eventX, eventY, event.getEventTime());
        }
    }

    private void onTouchUp(MotionEvent event, float eventX, float eventY) {
        setEraserPosition(event, eventX, eventY);
        setInkPosition(event, eventX, eventY);
        drawingCurve.addPoint(eventX, eventY, event.getEventTime());
        drawingCurve.onTouchUp(eventX, eventY);
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
            int color = drawingCurve.getPixel(Math.round(eventX), Math.round(eventY));
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
            curPaint.set(eventBrushChosen.paint);
            curPaint.setColor(currentStrokeColor);
        }

        curPaint.setStrokeWidth(eventBrushChosen.thickness);
    }

    public void onEvent(EventRedo eventRedo) {
        drawingCurve.redo();
        invalidate(drawingCurve.getDirtyRect());
    }

    public void onEvent(EventUndo eventUndo) {
        drawingCurve.undo();
        invalidate(drawingCurve.getDirtyRect());
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

        if (cachedBitmap != null) {
            cachedBitmap.recycle();
            cachedBitmap = null;
        }

        drawingCurve.hardResetWithAnimatedColor(this, currentBackgroundColor, color);

        currentBackgroundColor = color;
        invalidate();
    }

    public float getBrushWidth() { return drawingCurve.getStaticStrokeWidth(); }

    public Bitmap getDrawingBitmap() {
        return drawingCurve.getBitmap();
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
                ServiceBitmapUtils.compressBitmapAsByteArray(drawingCurve.getBitmap())));
        store.setLastBackgroundColor(currentBackgroundColor);
        return super.onSaveInstanceState();
    }

}