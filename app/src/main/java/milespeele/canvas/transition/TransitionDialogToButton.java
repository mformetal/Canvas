package milespeele.canvas.transition;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.Context;
import android.graphics.Path;
import android.transition.ArcMotion;
import android.transition.ChangeBounds;
import android.transition.TransitionValues;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;

import java.util.List;

import milespeele.canvas.R;
import milespeele.canvas.util.ViewUtils;
import milespeele.canvas.view.ViewCanvasLayout;
import milespeele.canvas.view.ViewCanvasSurface;
import milespeele.canvas.view.ViewRoundedFrameLayout;

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
        int endColor = context.getResources().getColor(R.color.accent);

        List<View> views = getTargets();
        View fab = views.get(0);
        ViewRoundedFrameLayout fabFrame = (ViewRoundedFrameLayout) views.get(1);
        ViewCanvasLayout layout = (ViewCanvasLayout) views.get(2);

        float translationX = fab.getX() - fabFrame.getWidth() / 2 + fab.getWidth() * .1f;
        float translationY = fab.getY() + fab.getHeight() * 3.25f;

        Animator alpha = ObjectAnimator.ofArgb(layout, ViewCanvasLayout.ALPHA, 0);

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
        animatorSet.playTogether(alpha, background, scale, position);
        animatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                fabFrame.setVisibility(View.GONE);
                fab.setVisibility(View.VISIBLE);

                for (int x = 0; x < layout.getChildCount(); x++) {
                    View v = layout.getChildAt(x);
                    if (!(v instanceof ViewRoundedFrameLayout)) {
                        enableView(v);
                    }
                }
            }
        });
        animatorSet.setDuration(350);
        animatorSet.setInterpolator(new AccelerateDecelerateInterpolator());

        return animatorSet;
    }

    private void enableView(View v) {
        v.setEnabled(true);

        if (v instanceof ViewGroup) {
            ViewGroup vg = (ViewGroup) v;
            for (int i = 0; i < vg.getChildCount(); i++) {
                View child = vg.getChildAt(i);
                enableView(child);
            }
        }
    }
}
