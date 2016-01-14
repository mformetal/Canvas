package miles.scribble.ui.widget;

import android.content.Context;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by mbpeele on 9/4/15.
 */
public class FabMenuBehavior extends CoordinatorLayout.Behavior<ViewGroup> {

    public FabMenuBehavior(Context context, AttributeSet attrs) {}

    @Override
    public boolean layoutDependsOn(CoordinatorLayout parent, ViewGroup child, View dependency) {
        return dependency instanceof Snackbar.SnackbarLayout;
    }

    @Override
    public boolean onDependentViewChanged(CoordinatorLayout parent, ViewGroup child, View dependency) {
        float translationY = Math.max(0, dependency.getHeight() - dependency.getTranslationY());
        child.setTranslationY(-translationY);
        return true;
    }
}
