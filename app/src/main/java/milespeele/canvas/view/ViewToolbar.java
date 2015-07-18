package milespeele.canvas.view;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import milespeele.canvas.R;

/**
 * Created by Miles Peele on 7/18/2015.
 */
public class ViewToolbar extends Toolbar {

    public ViewToolbar(Context context) {
        super(context);
        init();
    }

    public ViewToolbar(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ViewToolbar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setTitle(getResources().getString(R.string.app_name));
    }
}
