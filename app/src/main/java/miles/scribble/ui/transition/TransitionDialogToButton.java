package miles.scribble.ui.transition;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.app.Activity;
import android.content.Context;
import android.graphics.Path;
import android.graphics.Point;
import android.transition.ArcMotion;
import android.transition.ChangeBounds;
import android.transition.TransitionValues;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;

import java.util.List;

import miles.scribble.R;
import miles.scribble.util.ViewUtils;
import miles.scribble.ui.widget.CanvasLayout;
import miles.scribble.ui.widget.RoundedFrameLayout;

/**
 * Created by mbpeele on 12/30/15.
 */
public class TransitionDialogToButton extends ChangeBounds {

    private Context context;

    public TransitionDialogToButton(Context context) {
        this.context = context;
    }

    @Override
    public Animator createAnimator(ViewGroup sceneRoot, TransitionValues startValues, TransitionValues endValues) {
        super.createAnimator(sceneRoot, startValues, endValues);

        int startColor = context.getResources().getColor(R.color.primary_dark);
        int endColor = context.getResources().getColor(R.color.half_opacity_gray);

        List<View> views = getTargets();
        final View fab = views.get(0);
        final RoundedFrameLayout fabFrame = (RoundedFrameLayout) views.get(1);
        CanvasLayout layout = (CanvasLayout) views.get(2);

        Point size = new Point();
        ((Activity) context).getWindowManager().getDefaultDisplay().getSize(size);
        float yDiff = layout.getHeight() - size.y;

        float translationX = fab.getX() - fabFrame.getWidth() / 2 + fab.getWidth() * .1f;
        float translationY = fab.getY() + fab.getHeight() * 3.25f + yDiff;

        Animator background = ObjectAnimator.ofArgb(fabFrame,
                ViewUtils.BACKGROUND, startColor, endColor)
                .setDuration(450);

        ArcMotion arcMotion = new ArcMotion();
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
        animatorSet.playTogether(background, scale, position);
        animatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                fabFrame.setVisibility(View.GONE);
                fab.setVisibility(View.VISIBLE);
            }
        });
        animatorSet.setDuration(350);
        animatorSet.setInterpolator(new AccelerateDecelerateInterpolator());

        return animatorSet;
    }
}