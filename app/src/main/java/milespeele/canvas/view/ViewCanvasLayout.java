package milespeele.canvas.view;

import android.content.Context;
import android.support.design.widget.CoordinatorLayout;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

import butterknife.ButterKnife;
import butterknife.InjectView;
import milespeele.canvas.R;

/**
 * Created by Miles Peele on 7/10/2015.
 */
public class ViewCanvasLayout extends CoordinatorLayout {

    public ViewCanvasLayout(Context context) {
        super(context);
        init();
    }

    public ViewCanvasLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ViewCanvasLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
    }

}