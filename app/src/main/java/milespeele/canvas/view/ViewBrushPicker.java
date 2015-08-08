package milespeele.canvas.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.LinearLayout;

/**
 * Created by milespeele on 8/8/15.
 */
public class ViewBrushPicker extends LinearLayout {

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

    private void init() {

    }
}
