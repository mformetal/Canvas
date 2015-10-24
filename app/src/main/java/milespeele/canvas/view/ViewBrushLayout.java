package milespeele.canvas.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Paint;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.SeekBar;

import butterknife.Bind;
import butterknife.ButterKnife;
import milespeele.canvas.R;

/**
 * Created by milespeele on 8/8/15.
 */
public class ViewBrushLayout extends LinearLayout implements SeekBar.OnSeekBarChangeListener {

    @Bind(R.id.fragment_brush_picker_view_example) ViewBrushLayoutSizer example;
    @Bind(R.id.fragment_brush_picker_view_sizer) SeekBar sizer;
    @Bind(R.id.fragment_brush_picker_view_recycler) ViewPaintExamplesRecycler recycler;

    private final static int MAX_THICKNESS = 50;
    private float thickness;
    private Paint lastSelectedPaint;

    public ViewBrushLayout(Context context) {
        super(context);
        init();
    }

    public ViewBrushLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ViewBrushLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public ViewBrushLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        setOrientation(VERTICAL);
        setClipChildren(true);
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
    }

    public void setInitialValues(float thickness, int color) {
        recycler.setColor(color);
        example.changePaintColor(color);
        example.onThicknessChanged(thickness);
        sizer.setProgress(Math.round(thickness));
    }

    public float getThickness() {
        return thickness;
    }

    public Paint getLastSelectedPaint() { return lastSelectedPaint; }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (progress != 2) {
            thickness = progress;
            example.onThicknessChanged(progress);
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }
}
