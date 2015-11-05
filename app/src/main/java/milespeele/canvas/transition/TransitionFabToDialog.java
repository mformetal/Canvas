package milespeele.canvas.transition;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.Context;
import android.graphics.Color;
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
import milespeele.canvas.util.Logg;
import milespeele.canvas.util.ViewUtils;
import milespeele.canvas.view.ViewFab;

/**
 * Created by mbpeele on 11/4/15.
 */
public class TransitionFabToDialog extends ChangeBounds {

    private static final String COLOR = "milespeele.canvas:transitionFabToDialog:color";
    private static final String X = "milespeele.canvas:transitionFabToDialog:x";
    private static final String Y = "milespeele.canvas:transitionFabToDialog:y";
    private static final String[] PROPERTIES = {COLOR, X, Y};
    private int endColor;

    public TransitionFabToDialog(int endColor) {
        this.endColor = endColor;
    }

    @Override
    public void captureStartValues(TransitionValues transitionValues) {
        super.captureStartValues(transitionValues);
        transitionValues.values.put(COLOR,
                transitionValues.view.getContext().getResources().getColor(R.color.accent));
    }

    @Override
    public void captureEndValues(TransitionValues transitionValues) {
        super.captureEndValues(transitionValues);
        transitionValues.values.put(COLOR, endColor);
    }

    @Override
    public String[] getTransitionProperties() {
        return PROPERTIES;
    }

    @Override
    public Animator createAnimator(ViewGroup sceneRoot, TransitionValues startValues, TransitionValues endValues) {
        Integer startColor = (Integer) startValues.values.get(COLOR);
        Integer endColor = (Integer) endValues.values.get(COLOR);

        List<View> views = getTargets();
        ViewFab fab = (ViewFab) views.get(0);
        FrameLayout fabFrame = (FrameLayout) views.get(1);

        float fabCenterX = (fab.getLeft() + fab.getRight()) / 2;
        float fabCenterY = (fab.getTop() + fab.getBottom()) / 2;
        float translationX = fabCenterX - fabFrame.getWidth() / 2 - fab.getWidth();
        float translationY = fabCenterY + fab.getHeight() * 2;

        fabFrame.setScaleX((float) fab.getWidth() / (float) fabFrame.getWidth());
        fabFrame.setScaleY((float) fab.getHeight() / (float) fabFrame.getHeight());
        fabFrame.setTranslationX(translationX);
        fabFrame.setTranslationY(translationY);
        fabFrame.setVisibility(View.VISIBLE);

        fabFrame.animate().alpha(1f);

        Animator fade = ObjectAnimator.ofFloat(fab, View.ALPHA, 0f).setDuration(50);

        Animator reveal = ViewAnimationUtils.createCircularReveal(
                fabFrame,
                fabFrame.getWidth() / 2,
                fabFrame.getHeight() / 2,
                fab.getWidth() / 2,
                (int) Math.hypot(fabFrame.getWidth() / 2, fabFrame.getHeight() / 2))
                .setDuration(350);

        Animator background = ObjectAnimator.ofArgb(fabFrame,
                ViewUtils.BACKGROUND_PROPERTY, startColor, endColor)
                .setDuration(350);

        ArcMotion arcMotion = new ArcMotion();
        arcMotion.setMinimumVerticalAngle(70f);
        Path motionPath = arcMotion.getPath(translationX, translationY, 0, 0);
        Animator position = ObjectAnimator.ofFloat(fabFrame, View.TRANSLATION_X, View
                .TRANSLATION_Y, motionPath)
                .setDuration(350);

        PropertyValuesHolder scalerX = PropertyValuesHolder.ofFloat("scaleX",
                fabFrame.getScaleX(), 1f);
        PropertyValuesHolder scalerY = PropertyValuesHolder.ofFloat("scaleY",
                fabFrame.getScaleX(), 1f);
        ObjectAnimator scale = ObjectAnimator.ofPropertyValuesHolder(fabFrame, scalerX, scalerY)
                .setDuration(350);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(fade, reveal, background, scale, position);
        animatorSet.setDuration(350);
        animatorSet.setInterpolator(new AccelerateDecelerateInterpolator());

        return animatorSet;
    }
}
