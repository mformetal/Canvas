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
public class ViewBrushPickerLayout extends LinearLayout implements SeekBar.OnSeekBarChangeListener {

    @Bind(R.id.fragment_brush_picker_view_example) ViewBrushPickerCurrentBrush example;
    @Bind(R.id.fragment_brush_picker_view_sizer) SeekBar sizer;
    @Bind(R.id.fragment_brush_picker_view_recycler) ViewPaintExamplesRecycler recycler;

    private final static int MAX_THICKNESS = 100;
    private Paint lastSelectedPaint = new Paint();

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
        lastSelectedPaint.set(paint);
        example.changePaint(paint);
    }

    public void setInitialValues(Paint paint) {
        float thickness = paint.getStrokeWidth();

        lastSelectedPaint.set(paint);

        recycler.setColor(paint.getColor());

        example.setInitialPaint(paint);

        sizer.setProgress(Math.round(thickness));
    }

    public Paint getLastSelectedPaint() { return lastSelectedPaint; }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (progress > 1) {
            example.onThicknessChanged(progress);
            lastSelectedPaint.setStrokeWidth((float) progress);
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }
}
