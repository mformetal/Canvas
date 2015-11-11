package milespeele.canvas.view;

import android.content.Context;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Switch;
import android.widget.ToggleButton;

import milespeele.canvas.util.TextUtils;

/**
 * Created by mbpeele on 11/10/15.
 */
public class ViewTypefaceToggleButton extends ToggleButton {

    private Paint textPaint;

    public ViewTypefaceToggleButton(Context context) {
        super(context);
    }

    public ViewTypefaceToggleButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ViewTypefaceToggleButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public ViewTypefaceToggleButton(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    private void init() {
        if (isInEditMode()) {
            return;
        }

        setTypeface(TextUtils.getStaticTypeFace(getContext(), "Roboto.ttf"));
    }
}
