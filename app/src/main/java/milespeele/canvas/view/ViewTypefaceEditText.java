package milespeele.canvas.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.EditText;

import milespeele.canvas.util.FontUtils;

/**
 * Created by mbpeele on 9/2/15.
 */
public class ViewTypefaceEditText extends EditText {

    public ViewTypefaceEditText(Context context) {
        super(context);
    }

    public ViewTypefaceEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ViewTypefaceEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public ViewTypefaceEditText(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    private void init() {
        setTypeface(FontUtils.getStaticTypeFace(getContext(), "Roboto.ttf"));
    }

    public String getTextAsString() { return getText().toString(); }
}
