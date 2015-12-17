package milespeele.canvas.transition;

import android.app.Fragment;
import android.content.Context;
import android.graphics.Color;
import android.support.design.widget.CoordinatorLayout;
import android.transition.TransitionManager;
import android.widget.FrameLayout;

import milespeele.canvas.R;
import milespeele.canvas.view.ViewFab;

/**
 * Created by mbpeele on 11/5/15.
 */
public class TransitionHelper {

    public static void makeFabDialogTransitions(Context context, ViewFab fab, FrameLayout fabFrame, Fragment fragment) {
        TransitionFabToDialog transitionFabToDialog = new TransitionFabToDialog(context);
        transitionFabToDialog.addTarget(fab);
        transitionFabToDialog.addTarget(fabFrame);
        transitionFabToDialog.addTarget((CoordinatorLayout) fabFrame.getParent());

        TransitionDialogToFab transitionDialogToFab = new TransitionDialogToFab(context);
        transitionDialogToFab.addTarget(fab);
        transitionDialogToFab.addTarget(fabFrame);
        transitionDialogToFab.addTarget((CoordinatorLayout) fabFrame.getParent());

        fragment.setEnterTransition(transitionFabToDialog);
        fragment.setReturnTransition(transitionDialogToFab);
        fragment.setExitTransition(transitionDialogToFab);
    }
}
