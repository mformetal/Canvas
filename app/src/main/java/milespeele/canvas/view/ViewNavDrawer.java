package milespeele.canvas.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;

/**
 * Created by mbpeele on 1/8/16.
 */
public class ViewNavDrawer extends ViewGroup {

    private boolean isAnimating = false;

    public ViewNavDrawer(Context context) {
        super(context);
        init();
    }

    public ViewNavDrawer(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ViewNavDrawer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public ViewNavDrawer(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {

    }

    public void setAnimating(boolean bool) {
        isAnimating = bool;
    }

    public boolean isAnimating() {
        return isAnimating;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {

    }
}
