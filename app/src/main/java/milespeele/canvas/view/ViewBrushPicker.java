package milespeele.canvas.view;

import android.annotation.TargetApi;
import android.content.Context;
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
public class ViewBrushPicker extends LinearLayout implements SeekBar.OnSeekBarChangeListener {

    @Bind(R.id.fragment_brush_picker_view_size) ViewBrushSize sizer;
    @Bind(R.id.fragment_brush_picker_view_seekbar) SeekBar change;

    private float thickness;

    public ViewBrushPicker(Context context) {
        super(context);
        init();
    }

    public ViewBrushPicker(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ViewBrushPicker(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public ViewBrushPicker(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        ButterKnife.bind(this);
    }

    private void init() {

    }

    public void setThickness(float thickness) {
        sizer.onThicknessChanged(thickness);
        change.setProgress(Math.round(thickness));
        change.setOnSeekBarChangeListener(this);
    }

    public float getThickness() {
        return thickness;
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        thickness = progress;
        sizer.onThicknessChanged(progress);
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }
}
