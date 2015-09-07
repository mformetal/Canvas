package milespeele.canvas.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.LinearLayout;

/**
 * Created by Miles Peele on 9/6/2015.
 */
public class ViewPantExampleLayout extends LinearLayout {

    public ViewPantExampleLayout(Context context) {
        super(context);
        init();
    }

    public ViewPantExampleLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ViewPantExampleLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public ViewPantExampleLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {

    }
}
