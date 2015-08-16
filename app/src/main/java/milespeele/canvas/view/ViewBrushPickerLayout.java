package milespeele.canvas.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.RelativeLayout;
import android.widget.SeekBar;

import butterknife.Bind;
import butterknife.ButterKnife;
import milespeele.canvas.R;

/**
 * Created by milespeele on 8/8/15.
 */
public class ViewBrushPickerLayout extends RelativeLayout implements SeekBar.OnSeekBarChangeListener {

    @Bind(R.id.fragment_brush_picker_view_example) ViewBrushPickerSizeAlpha example;
    @Bind(R.id.fragment_brush_picker_view_sizer) SeekBar sizer;
    @Bind(R.id.fragment_brush_picker_view_alphaer) SeekBar alphaer;

    private final static int MAX_THICKNESS = 75;
    private final static int MAX_ALPHA = 255;
    private float thickness;
    private int alpha;

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

    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        ButterKnife.bind(this);
        sizer.setMax(MAX_THICKNESS);
        sizer.setOnSeekBarChangeListener(this);
        alphaer.setMax(MAX_ALPHA);
        alphaer.setOnSeekBarChangeListener(this);
    }

    public void setInitialValues(float thickness, int alpha) {
        example.onValuesChanged(thickness, alpha);
        sizer.setProgress(Math.round(thickness));
        alphaer.setProgress(alpha);
    }

    public float getThickness() {
        return thickness;
    }

    public int getChosenAlpha() { return alpha; }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (seekBar == sizer) {
            thickness = progress;
            example.onThicknessChanged(progress);
        } else {
            alpha = progress;
            example.onAlphaChanged(alpha);
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }
}
