package milespeele.canvas.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;

import milespeele.canvas.R;

/**
 * Created by Miles Peele on 7/9/2015.
 */
public class ViewFabMenu extends ViewGroup {

    private int mButtonSpacing;
    private int mLabelsMargin;
    private int mLabelsVerticalOffset;

    public ViewFabMenu(Context context) {
        this(context, null);
    }

    public ViewFabMenu(Context context, AttributeSet attrs) {
        super(context, attrs);
        //init(context, attrs);
    }

    public ViewFabMenu(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        //init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        mButtonSpacing =
                (int) (getResources().getDimension(R.dimen.fab_actions_spacing) -
                getResources().getDimension(R.dimen.fab_shadow_radius) -
                getResources().getDimension(R.dimen.fab_shadow_offset));
        mLabelsMargin = getResources().getDimensionPixelSize(R.dimen.fab_labels_margin);
        mLabelsVerticalOffset = getResources().getDimensionPixelSize(R.dimen.fab_shadow_offset);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {

    }
}
