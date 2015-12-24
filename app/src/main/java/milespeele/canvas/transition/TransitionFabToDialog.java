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
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.FrameLayout;

import java.util.List;

import milespeele.canvas.R;
import milespeele.canvas.util.AbstractAnimatorListener;
import milespeele.canvas.util.Logg;
import milespeele.canvas.util.ViewUtils;
import milespeele.canvas.view.ViewCanvasLayout;
import milespeele.canvas.view.ViewCanvasSurface;
import milespeele.canvas.view.ViewFab;
import milespeele.canvas.view.ViewRoundedFrameLayout;

/**
 * Created by mbpeele on 11/4/15.
 */
public class TransitionFabToDialog extends ChangeBounds {

    private Context context;

    public TransitionFabToDialog(Context context) {
        this.context = context;
    }

    @Override
    public Animator createAnimator(ViewGroup sceneRoot, TransitionValues startValues, TransitionValues endValues) {
        int startColor = context.getResources().getColor(R.color.accent);
        int endColor = context.getResources().getColor(R.color.primary_dark);

        List<View> views = getTargets();
        ViewFab fab = (ViewFab) views.get(0);
        ViewRoundedFrameLayout fabFrame = (ViewRoundedFrameLayout) views.get(1);
        ViewCanvasLayout layout = (ViewCanvasLayout) views.get(2);

        float fabRadius = ViewUtils.radius(fab);
        float fabCenterX = fab.getX() + fabRadius;
        float fabCenterY = fab.getY() + fabRadius;
        float translationX = fabCenterX - fabFrame.getWidth() / 2 - (3 * fab.getWidth()) / 4;
        float translationY = fabCenterY + fab.getHeight() * 2;

        fabFrame.setScaleX((float) fab.getWidth() / (float) fabFrame.getWidth());
        fabFrame.setScaleY((float) fab.getHeight() / (float) fabFrame.getHeight());
        fabFrame.setTranslationX(translationX);
        fabFrame.setTranslationY(translationY);
        fabFrame.setVisibility(View.VISIBLE);

        fabFrame.setCorner(fabFrame.getWidth());

        Animator corner = ObjectAnimator.ofFloat(fabFrame,
                ViewRoundedFrameLayout.CORNERS, fabFrame.getWidth(), 0)
                .setDuration(350);

        Animator alpha = ObjectAnimator.ofArgb(layout, ViewCanvasLayout.ALPHA, 128);

        Animator background = ObjectAnimator.ofArgb(fabFrame,
                ViewUtils.BACKGROUND, startColor, endColor)
                .setDuration(350);

        ArcMotion arcMotion = new ArcMotion();
        arcMotion.setMinimumVerticalAngle(70f);
        Path motionPath = arcMotion.getPath(translationX, translationY, 0, 0);
        Animator position = ObjectAnimator.ofFloat(fabFrame, View.TRANSLATION_X, View
                .TRANSLATION_Y, motionPath)
                .setDuration(350);

        ObjectAnimator scale = ObjectAnimator.ofPropertyValuesHolder(fabFrame,
                PropertyValuesHolder.ofFloat(View.SCALE_X, fabFrame.getScaleX(), 1f),
                PropertyValuesHolder.ofFloat(View.SCALE_Y, fabFrame.getScaleX(), 1f))
                .setDuration(350);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(alpha, background, position);
        animatorSet.addListener(new AbstractAnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                fab.setVisibility(View.GONE);

                for (int x = 0; x < layout.getChildCount(); x++) {
                    View v = layout.getChildAt(x);
                    if (!(v instanceof FrameLayout)) {
                        disableView(v);
                    }
                }
            }
        });
        animatorSet.setDuration(350);
        animatorSet.setInterpolator(new AccelerateDecelerateInterpolator());

        return animatorSet;
    }

    public void disableView(View v) {
        v.setEnabled(false);

        if (v instanceof ViewCanvasSurface) {
            v.setOnTouchListener(null);
        }

        if (v instanceof ViewGroup) {
            ViewGroup vg = (ViewGroup) v;
            for (int i = 0; i < vg.getChildCount(); i++) {
                View child = vg.getChildAt(i);
                disableView(child);
            }
        }
    }
}
