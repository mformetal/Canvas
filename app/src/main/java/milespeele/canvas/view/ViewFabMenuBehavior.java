package milespeele.canvas.view;

import android.content.Context;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.util.AttributeSet;
import android.view.View;

import milespeele.canvas.util.Logg;

/**
 * Created by Miles Peele on 9/5/2015.
 */
public class ViewFabMenuBehavior extends CoordinatorLayout.Behavior<ViewFabMenu> {

    public ViewFabMenuBehavior(Context context, AttributeSet attributeSet) {}

    @Override
    public boolean layoutDependsOn(CoordinatorLayout parent, ViewFabMenu child, View dependency) {
        return dependency instanceof Snackbar.SnackbarLayout;
    }

    @Override
    public boolean onDependentViewChanged(CoordinatorLayout parent, ViewFabMenu child, View dependency) {
        child.setTranslationY(Math.min(0, dependency.getTranslationY() - dependency.getHeight()));
        return true;
    }
}
