package milespeele.canvas.view;

import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.Button;

import milespeele.canvas.R;

/**
 * Created by Miles Peele on 8/2/2015.
 */
public class ViewBottomSheetMenuButton extends Button {

    private Path path;
    private Paint paint;

    private boolean shouldAnimate = false;

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
