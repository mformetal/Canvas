package milespeele.canvas.transition;

import android.app.Fragment;
import android.content.Context;
import android.graphics.Color;
import android.transition.TransitionManager;
import android.widget.FrameLayout;

import milespeele.canvas.R;
import milespeele.canvas.view.ViewFab;

/**
 * Created by mbpeele on 11/5/15.
 */
public class TransitionHelper {

    public static void makeFabDialogTransitions(Context context, ViewFab fab, FrameLayout fabFrame, Fragment fragment) {
        int endColor = context.getResources().getColor(R.color.primary_dark);
        if (fab.getId() == R.id.menu_stroke_color || fab.getId() == R.id.menu_new_canvas) {
            endColor = Color.TRANSPARENT;
        }
        TransitionFabToDialog transitionFabToDialog = new TransitionFabToDialog(endColor);
        transitionFabToDialog.addTarget(fab);
        transitionFabToDialog.addTarget(fabFrame);

        TransitionDialogToFab transitionDialogToFab = new TransitionDialogToFab(endColor);
        transitionDialogToFab.addTarget(fab);
        transitionDialogToFab.addTarget(fabFrame);

        fragment.setEnterTransition(transitionFabToDialog);
        fragment.setReturnTransition(transitionDialogToFab);
    }
}
