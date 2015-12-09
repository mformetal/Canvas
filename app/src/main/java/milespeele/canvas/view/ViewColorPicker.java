package milespeele.canvas.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;

import java.util.HashMap;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;
import milespeele.canvas.R;
import milespeele.canvas.util.Logg;
import milespeele.canvas.util.TextUtils;
import milespeele.canvas.util.ViewUtils;

/**
 * Created by mbpeele on 12/4/15.
 */
public class ViewColorPicker extends FrameLayout {

    @Bind(R.id.fragment_color_picker_pos_button) ViewTypefaceButton posButton;
    @Bind(R.id.fragment_color_picker_neg_button) ViewTypefaceButton negButton;

    private final static float START_X_MULT = .1f;
    private final static float END_X_MULT = .9f;
    private final static float LINE_THICKNESS = 15f;
    private float circleRadius;
    private float slope, end, start;
    private float[] barY;
    private float redPos, bluePos, greenPos;
    private int curColor, curRed, curBlue, curGreen;

    private Paint paint;

    public ViewColorPicker(Context context) {
        super(context);
        init();
    }

    public ViewColorPicker(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ViewColorPicker(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public ViewColorPicker(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        paint = new Paint();
        paint.setStrokeWidth(LINE_THICKNESS);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setTypeface(TextUtils.getStaticTypeFace(getContext(), "Roboto.ttf"));

        setWillNotDraw(false);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int height = getDefaultSize(getMinimumHeight(), heightMeasureSpec);
        int width = getDefaultSize(getMinimumWidth(), widthMeasureSpec);

        for (int i = 0; i < getChildCount(); i++) {
            measureChildWithMargins(getChildAt(i), widthMeasureSpec, 0, heightMeasureSpec, 0);
        }

        setMeasuredDimension(width, Math.round(ViewUtils.getScreenHeight(getContext()) * .7f));
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        TextUtils.adjustTextScale(paint, "0xFFFFFF", w / 2, getPaddingLeft(), getPaddingRight());
        TextUtils.adjustTextSize(paint, "0xFFFFFF", h / 2);
        circleRadius = (h * .9f) / 50;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        paint.setColor(curColor);
        canvas.drawRect(0, 0, canvas.getWidth(), canvas.getHeight() / 2, paint);

        paint.setColor(ViewUtils.getComplimentColor(curColor));
        String colorText = ViewUtils.colorToHexString(curColor);
        canvas.drawText(colorText, (canvas.getWidth() - paint.measureText(colorText)) / 2,
                canvas.getHeight() * (5f / 16f), paint);

        if (barY == null) {
            final float heightForBars = posButton.getTop() - canvas.getHeight() / 2;

            barY = new float[3];
            barY[0] = canvas.getHeight() / 2 + heightForBars * .25f;
            barY[1] = canvas.getHeight() / 2 + heightForBars * .5f;
            barY[2] = canvas.getHeight() / 2 + heightForBars * .75f;
        }

        paint.setColor(Color.RED);
        canvas.drawLine(canvas.getWidth() * START_X_MULT, barY[0], canvas.getWidth() * END_X_MULT, barY[0], paint);
        canvas.drawCircle(redPos, barY[0], circleRadius, paint);

        paint.setColor(Color.GREEN);
        canvas.drawLine(canvas.getWidth() * START_X_MULT, barY[1], canvas.getWidth() * END_X_MULT, barY[1], paint);
        canvas.drawCircle(greenPos, barY[1], circleRadius, paint);

        paint.setColor(Color.BLUE);
        canvas.drawLine(canvas.getWidth() * START_X_MULT, barY[2], canvas.getWidth() * END_X_MULT, barY[2], paint);
        canvas.drawCircle(bluePos, barY[2], circleRadius, paint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();

        switch (action & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                onTouchDown(event);
                break;

            case MotionEvent.ACTION_MOVE:
                onTouchMove(event);
                break;

            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                onTouchUp(event);
                break;

            case MotionEvent.ACTION_POINTER_UP:
                break;
        }

        return true;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        ButterKnife.bind(this);
    }

    public void setTitleColor(int color) {
        curColor = color;
        curRed = Color.red(curColor);
        curBlue = Color.blue(curColor);
        curGreen = Color.green(curColor);

        if (getWidth() == 0) {
            final ViewTreeObserver observer = getViewTreeObserver();
            observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    if (observer.isAlive()) {
                        observer.removeOnGlobalLayoutListener(this);
                    } else {
                        getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    }

                    end = getWidth() * END_X_MULT;
                    start = getWidth() * START_X_MULT;
                    slope = (end - start) / 255f;

                    redPos = slope * (curRed) + start;
                    bluePos = slope * (curBlue) + start;
                    greenPos = slope * (curGreen) + start;
                }
            });
        }
    }

    private void onTouchDown(MotionEvent event) {
        getColorBasedOnPosition(event.getX(), event.getY());
    }

    private void onTouchMove(MotionEvent event) {
        getColorBasedOnPosition(event.getX(), event.getY());
    }

    private void onTouchUp(MotionEvent event) {
//        getColorBasedOnPosition(event.getX(), event.getY());
    }

    private void getColorBasedOnPosition(float x, float y) {
        for (int ndx = 0; ndx < barY.length; ndx++) {
            float linePos = barY[ndx];
            if (linePos - LINE_THICKNESS * 4 <= y && y <= linePos + LINE_THICKNESS * 4) {
                float value = (x - start) / slope;

                float pos;
                int color;

                if (x <= start) {
                    pos = start;
                    color = 0;
                } else if (x >= end) {
                    pos = end;
                    color = 255;
                } else {
                    pos = x;
                    color = Math.round(value);
                }

                switch (ndx) {
                    case 0:
                        redPos = pos;
                        curRed = color;
                        break;
                    case 1:
                        greenPos = pos;
                        curGreen = color;
                        break;
                    case 2:
                        bluePos = pos;
                        curBlue = color;
                        break;
                }

                curColor = Color.rgb(curRed, curGreen, curBlue);
                invalidate();
                break;
            }
        }
    }

    public int getSelectedColor() {
        return curColor;
    }
}
