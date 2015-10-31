package milespeele.canvas.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.Button;

import milespeele.canvas.util.TextUtils;

/**
 * Created by mbpeele on 9/2/15.
 */
public class ViewTypefaceButton extends Button {

    public ViewTypefaceButton(Context context) {
        super(context);
    }

    public ViewTypefaceButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ViewTypefaceButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public ViewTypefaceButton(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    private void init() {
        setTypeface(TextUtils.getStaticTypeFace(getContext(), "Roboto.ttf"));
    }
}
