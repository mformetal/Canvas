package milespeele.canvas.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.Button;

/**
 * Created by Miles Peele on 8/2/2015.
 */
public class ViewBottomSheetMenuButton extends Button {

    public ViewBottomSheetMenuButton(Context context) {
        super(context);
        init();
    }

    public ViewBottomSheetMenuButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ViewBottomSheetMenuButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public ViewBottomSheetMenuButton(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
    }

}
