package milespeele.canvas.view;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;

/**
 * Created by Miles Peele on 9/17/2015.
 */
public class Recycler extends RecyclerView {

    public Recycler(Context context) {
        super(context);
        init();
    }

    public Recycler(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public Recycler(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        setClipChildren(false);
        setHasFixedSize(true);
    }
}
