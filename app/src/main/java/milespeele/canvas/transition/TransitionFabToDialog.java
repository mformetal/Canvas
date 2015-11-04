package milespeele.canvas.transition;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.graphics.Color;
import android.graphics.Path;
import android.transition.ArcMotion;
import android.transition.ChangeBounds;
import android.transition.TransitionValues;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;

import java.util.List;

import milespeele.canvas.R;
import milespeele.canvas.util.AbstractAnimatorListener;
import milespeele.canvas.util.Logg;
import milespeele.canvas.util.ViewUtils;
import milespeele.canvas.view.ViewFab;

/**
 * Created by mbpeele on 11/4/15.
 */
public class TransitionFabToDialog extends ChangeBounds {

    private static final int DURATION = 350;
    private static final String COLOR = "milespeele.canvas:transitionFabToDialog:color";
    private static final String START_X = "milespeele.canvas:transitionFabToDialog:startX";
    private static final String START_Y = "milespeele.canvas:transitionFabToDialog:startY";
    private static final String END_X = "milespeele.canvas:transitionFabToDialog:endX";
    private static final String END_Y = "milespeele.canvas:transitionFabToDialog:endY";
    private static final String[] TRANSITION_PROPERTIES = {
            COLOR,
            START_X,
            START_Y,
            END_X,
            END_Y
    };

    private int startColor, endColor;
    private int startX, startY;
    private int endX, endY;

    public TransitionFabToDialog(int startColor, int endColor, int startX, int startY, int endX, int endY) {
        super();
        this.startColor = startColor;
        this.endColor = endColor;
        this.startX = startX;
        this.startY = startY;
        this.endX = endX;
        this.endY = endY;
    }

    @Override
    public String[] getTransitionProperties() {
        return TRANSITION_PROPERTIES;
    }

    @Override
    public void captureStartValues(TransitionValues transitionValues) {
        super.captureStartValues(transitionValues);
        final View view = transitionValues.view;
        if (view.getWidth() <= 0 || view.getHeight() <= 0) {
            return;
        }
        transitionValues.values.put(COLOR, startColor);
        transitionValues.values.put(START_X, startX);
        transitionValues.values.put(START_Y, startY);
    }

    @Override
    public void captureEndValues(TransitionValues transitionValues) {
        super.captureEndValues(transitionValues);
        final View view = transitionValues.view;
        if (view.getWidth() <= 0 || view.getHeight() <= 0) {
            return;
        }
        transitionValues.values.put(COLOR, endColor);
        transitionValues.values.put(END_X, endX);
        transitionValues.values.put(END_Y, endY);
    }

    @Override
    public Animator createAnimator(final ViewGroup sceneRoot,
                                   TransitionValues startValues,
                                   final TransitionValues endValues) {
        if (startValues == null || endValues == null) {
            return null;
        }


        List<View> targets = getTargets();

        View fab = targets.get(0);
        View frame = targets.get(1);

        Animator backgroundColor = ObjectAnimator.ofArgb(frame,
                ViewUtils.BACKGROUND_PROPERTY, startColor, endColor)
                .setDuration(DURATION);

        ArcMotion arcMotion = new ArcMotion();
        arcMotion.setMinimumVerticalAngle(70f);
        Path motionPath = arcMotion.getPath(
                fab.getTranslationX(), fab.getTranslationY(),
                endX - startX - fab.getWidth() / 2, endY - startY);
        Animator position = ObjectAnimator.ofFloat(fab, View.TRANSLATION_X, View
                .TRANSLATION_Y, motionPath)
                .setDuration(DURATION);

        if (endValues.view instanceof ViewGroup) {
            ViewGroup vg = (ViewGroup) endValues.view;
            for (int i = 0; i < vg.getChildCount(); i++) {
                View v = vg.getChildAt(i);
                v.setAlpha(0f);
                v.animate()
                        .alpha(1f)
                        .setInterpolator(new AccelerateDecelerateInterpolator());
            }
        }

        AnimatorSet transition = new AnimatorSet();
        transition.playTogether(backgroundColor, position);
        transition.setDuration(DURATION);
        transition.addListener(new AbstractAnimatorListener() {
            @Override
            public void onAnimationEnd(Animator animation) {
                frame.setVisibility(View.VISIBLE);

                Animator backgroundColor = ObjectAnimator.ofArgb(frame,
                        ViewUtils.BACKGROUND_PROPERTY, startColor, endColor)
                        .setDuration(DURATION);

                Animator reveal = ViewAnimationUtils.createCircularReveal(
                        frame,
                        frame.getWidth() / 2,
                        frame.getHeight() / 2,
                        fab.getWidth(),
                        frame.getHeight())
                        .setDuration(DURATION);

                AnimatorSet set = new AnimatorSet();
                set.playTogether(backgroundColor, reveal);
                set.setInterpolator(new AccelerateDecelerateInterpolator());
                set.start();
            }
        });
        transition.setInterpolator(new AccelerateDecelerateInterpolator());
        return transition;
    }
}