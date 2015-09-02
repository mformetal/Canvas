package milespeele.canvas.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.TextView;

import milespeele.canvas.util.FontUtils;

/**
 * Created by mbpeele on 9/2/15.
 */
public class ViewTypefaceTextView extends TextView {

    public ViewTypefaceTextView(Context context) {
        super(context);
    }

    public ViewTypefaceTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ViewTypefaceTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public ViewTypefaceTextView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    private void init() {
        setTypeface(FontUtils.getStaticTypeFace(getContext(), "Roboto.ttfw"));
    }
}
