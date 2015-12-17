package milespeele.canvas.transition;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.Context;
import android.graphics.Path;
import android.transition.ArcMotion;
import android.transition.ChangeBounds;
import android.transition.TransitionValues;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.FrameLayout;

import java.util.List;

import milespeele.canvas.R;
import milespeele.canvas.util.AbstractAnimatorListener;
import milespeele.canvas.util.ViewUtils;
import milespeele.canvas.view.ViewCanvasLayout;
import milespeele.canvas.view.ViewCanvasSurface;
import milespeele.canvas.view.ViewFab;

/**
 * Created by mbpeele on 11/5/15.
 */
public class TransitionDialogToFab extends ChangeBounds {

    private Context context;

    public TransitionDialogToFab(Context context) {
        this.context = context;
    }

    @Override
    public Animator createAnimator(ViewGroup sceneRoot, TransitionValues startValues, TransitionValues endValues) {
        super.createAnimator(sceneRoot, startValues, endValues);

        int startColor = context.getResources().getColor(R.color.primary_dark);
        int endColor = context.getResources().getColor(R.color.accent);

        List<View> views = getTargets();
        ViewFab fab = (ViewFab) views.get(0);
        FrameLayout fabFrame = (FrameLayout) views.get(1);
        ViewCanvasLayout layout = (ViewCanvasLayout) views.get(2);

        float fabCenterX = (fab.getLeft() + fab.getRight()) / 2;
        float fabCenterY = (fab.getTop() + fab.getBottom()) / 2;
        float translationX = fabCenterX - fabFrame.getWidth() / 2 - fab.getWidth() * .75f;
        float translationY = fabCenterY + fab.getHeight() * 2;

        Animator alpha = ObjectAnimator.ofArgb(layout, ViewCanvasLayout.ALPHA, 0);

        Animator background = ObjectAnimator.ofArgb(fabFrame,
                ViewUtils.BACKGROUND, startColor, endColor)
                .setDuration(450);

        ArcMotion arcMotion = new ArcMotion();
        arcMotion.setMinimumVerticalAngle(70f);
        Path motionPath = arcMotion.getPath(0, 0, translationX, translationY);
        Animator position = ObjectAnimator.ofFloat(fabFrame, View.TRANSLATION_X, View
                .TRANSLATION_Y, motionPath)
                .setDuration(350);

        PropertyValuesHolder scalerX = PropertyValuesHolder.ofFloat("scaleX",
                fabFrame.getScaleX(), (float) fab.getWidth() / (float) fabFrame.getWidth());
        PropertyValuesHolder scalerY = PropertyValuesHolder.ofFloat("scaleY",
                fabFrame.getScaleX(), (float) fab.getHeight() / (float) fabFrame.getHeight());
        ObjectAnimator scale = ObjectAnimator.ofPropertyValuesHolder(fabFrame, scalerX, scalerY)
                .setDuration(350);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(alpha, background, scale, position);
        animatorSet.addListener(new AbstractAnimatorListener() {
            @Override
            public void onAnimationEnd(Animator animation) {
                fabFrame.setVisibility(View.GONE);
                fab.setVisibility(View.VISIBLE);

                for (int x = 0; x < layout.getChildCount(); x++) {
                    View v = layout.getChildAt(x);
                    if (!(v instanceof FrameLayout)) {
                        enableView(v);
                    }
                }

                ViewAnimationUtils.createCircularReveal(
                        fab,
                        fab.getWidth() / 2, fab.getHeight() / 2,
                        0, fab.getWidth() / 2)
                        .setDuration(150).start();
            }
        });
        animatorSet.setDuration(350);
        animatorSet.setInterpolator(new AccelerateDecelerateInterpolator());

        return animatorSet;
    }

    private void enableView(View v) {
        v.setEnabled(true);

        if (v instanceof ViewCanvasSurface) {
            v.setOnTouchListener((View.OnTouchListener) v);
        }

        if (v instanceof ViewGroup) {
            ViewGroup vg = (ViewGroup) v;
            for (int i = 0; i < vg.getChildCount(); i++) {
                View child = vg.getChildAt(i);
                enableView(child);
            }
        }
    }
}
