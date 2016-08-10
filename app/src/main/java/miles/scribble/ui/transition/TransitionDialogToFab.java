package miles.scribble.ui.transition;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
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
import android.view.ViewGroup;

import java.util.List;

import miles.scribble.R;
import miles.scribble.util.ViewUtils;
import miles.scribble.ui.widget.CanvasLayout;
import miles.scribble.ui.widget.RoundedFrameLayout;

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
        int endColor = Color.WHITE;

        List<View> views = getTargets();
        final View fab = views.get(0);
        final RoundedFrameLayout fabFrame = (RoundedFrameLayout) views.get(1);
        CanvasLayout layout = (CanvasLayout) views.get(2);

        float fabRadius = ViewUtils.radius(fab);
        float fabCenterX = fab.getX() + fabRadius;
        float fabCenterY = fab.getY() + fabRadius;
        float translationX = fabCenterX - fabFrame.getWidth() / 2 - fab.getWidth() * .75f;
        float translationY = fabCenterY + fab.getHeight() * 3.5f;

        Animator alpha = ObjectAnimator.ofFloat(layout.getChildAt(3), View.ALPHA, 1f).setDuration(350);

        Animator corner = ObjectAnimator.ofFloat(fabFrame,
                RoundedFrameLayout.CORNERS, 0, fabFrame.getWidth())
                .setDuration(350);
        corner.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                fabFrame.setAnimating(true);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                fabFrame.setAnimating(false);
            }
        });

        Animator background = ObjectAnimator.ofArgb(fabFrame,
                ViewUtils.BACKGROUND, startColor, endColor)
                .setDuration(350);

        ArcMotion arcMotion = new ArcMotion();
        arcMotion.setMinimumVerticalAngle(70f);
        arcMotion.setMinimumHorizontalAngle(15f);
        Path motionPath = arcMotion.getPath(0, 0, translationX, translationY);
        Animator position = ObjectAnimator.ofFloat(fabFrame, View.TRANSLATION_X, View
                .TRANSLATION_Y, motionPath)
                .setDuration(350);

        PropertyValuesHolder scalerX = PropertyValuesHolder.ofFloat(View.SCALE_X,
                fabFrame.getScaleX(), (float) fab.getWidth() / (float) fabFrame.getWidth());
        PropertyValuesHolder scalerY = PropertyValuesHolder.ofFloat(View.SCALE_Y,
                fabFrame.getScaleX(), (float) fab.getHeight() / (float) fabFrame.getHeight());
        ObjectAnimator scale = ObjectAnimator.ofPropertyValuesHolder(fabFrame, scalerX, scalerY)
                .setDuration(350);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(background, position, corner, alpha, scale);
        animatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                fabFrame.setVisibility(View.GONE);
                fab.setVisibility(View.VISIBLE);
            }
        });

        return animatorSet;
    }
}