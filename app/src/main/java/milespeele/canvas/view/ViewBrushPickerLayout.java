package milespeele.canvas.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Paint;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.SeekBar;

import butterknife.Bind;
import butterknife.ButterKnife;
import milespeele.canvas.R;
import milespeele.canvas.util.Logg;

/**
 * Created by milespeele on 8/8/15.
 */
public class ViewBrushPickerLayout extends LinearLayout implements SeekBar.OnSeekBarChangeListener {

    @Bind(R.id.fragment_brush_picker_view_example)
    ViewBrushPickerPaintExampleWidth example;
    @Bind(R.id.fragment_brush_picker_view_sizer) SeekBar sizer;

    private final static int MAX_THICKNESS = 120;
    private float thickness;
    private Paint lastSelectedPaint;

    public ViewBrushPickerLayout(Context context) {
        super(context);
        init();
    }

    public ViewBrushPickerLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ViewBrushPickerLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public ViewBrushPickerLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        setClipChildren(false);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        ButterKnife.bind(this);
        sizer.setMax(MAX_THICKNESS);
        sizer.setOnSeekBarChangeListener(this);
    }

    public void changeExamplePaint(Paint paint) {
        lastSelectedPaint = paint;
        example.changePaint(paint);
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            if (child instanceof ViewBrushPickerPaintExampleLayout) {
                ((ViewBrushPickerPaintExampleLayout) child).dehighlight();
            }
        }
    }

    public void setInitialValues(float thickness) {
        example.onThicknessChanged(thickness);
        sizer.setProgress(Math.round(thickness));
    }

    public float getThickness() {
        return thickness;
    }

    public Paint getLastSelectedPaint() { return lastSelectedPaint; }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        thickness = progress;
        example.onThicknessChanged(progress);
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            if (child instanceof ViewBrushPickerPaintExampleLayout) {
                ((ViewBrushPickerPaintExampleLayout) child).changeExamplePaintViewThickness(progress);
            }
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }
}
