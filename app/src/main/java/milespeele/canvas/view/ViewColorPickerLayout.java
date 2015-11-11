package milespeele.canvas.view;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import butterknife.Bind;
import butterknife.ButterKnife;
import milespeele.canvas.R;

/**
 * Created by mbpeele on 11/10/15.
 */
public class ViewColorPickerLayout extends LinearLayout {

    @Bind(R.id.fragment_color_picker_current_color) ViewTypefaceTextView currentColorView;

    private int currentColor;

    public ViewColorPickerLayout(Context context) {
        super(context);
    }

    public ViewColorPickerLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ViewColorPickerLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public ViewColorPickerLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        ButterKnife.bind(this);
    }

    public void setCurrentColor(int color) {
        currentColorView.setTextColor(Color.WHITE);
        currentColorView.setText(String.format("#%06X", (0xFFFFFF & color)));
        currentColorView.setBackgroundColor(color);
    }
}
