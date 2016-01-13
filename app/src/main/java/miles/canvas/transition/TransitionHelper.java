package miles.canvas.transition;

import android.app.Fragment;
import android.content.Context;
import android.support.design.widget.CoordinatorLayout;
import android.view.View;
import android.widget.FrameLayout;

/**
 * Created by mbpeele on 11/5/15.
 */
public class TransitionHelper {

    public static void makeFabDialogTransitions(Context context, View view, FrameLayout fabFrame, Fragment fragment) {
        TransitionFabToDialog transitionFabToDialog = new TransitionFabToDialog(context);
        transitionFabToDialog.addTarget(view);
        transitionFabToDialog.addTarget(fabFrame);
        transitionFabToDialog.addTarget((CoordinatorLayout) fabFrame.getParent());

        TransitionDialogToFab transitionDialogToFab = new TransitionDialogToFab(context);
        transitionDialogToFab.addTarget(view);
        transitionDialogToFab.addTarget(fabFrame);
        transitionDialogToFab.addTarget((CoordinatorLayout) fabFrame.getParent());

        fragment.setEnterTransition(transitionFabToDialog);
        fragment.setReturnTransition(transitionDialogToFab);
        fragment.setExitTransition(transitionDialogToFab);
    }

    public static void makeButtonDialogTransitions(Context context, View button, FrameLayout fragFrame, Fragment fragment) {
        TransitionButtonToDialog transitionButtonToDialog = new TransitionButtonToDialog(context);
        transitionButtonToDialog.addTarget(button);
        transitionButtonToDialog.addTarget(fragFrame);
        transitionButtonToDialog.addTarget(((CoordinatorLayout) fragFrame.getParent()));

        TransitionDialogToButton transitionDialogToButton = new TransitionDialogToButton(context);
        transitionDialogToButton.addTarget(button);
        transitionDialogToButton.addTarget(fragFrame);
        transitionDialogToButton.addTarget(((CoordinatorLayout) fragFrame.getParent()));

        fragment.setEnterTransition(transitionButtonToDialog);
        fragment.setReturnTransition(transitionDialogToButton);
        fragment.setExitTransition(transitionDialogToButton);
    }
}
