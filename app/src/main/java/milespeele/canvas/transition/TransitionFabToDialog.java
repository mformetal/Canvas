package milespeele.canvas.transition;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Path;
import android.graphics.Point;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.transition.ArcMotion;
import android.transition.ChangeBounds;
import android.transition.TransitionValues;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AnimationUtils;
import android.view.animation.AnticipateOvershootInterpolator;
import android.view.animation.LinearInterpolator;

import java.util.List;

import milespeele.canvas.R;
import milespeele.canvas.util.Logg;
import milespeele.canvas.util.ViewUtils;
import milespeele.canvas.view.ViewCanvasLayout;
import milespeele.canvas.view.ViewCanvasSurface;
import milespeele.canvas.view.ViewFab;
import milespeele.canvas.view.ViewRoundedFrameLayout;
import milespeele.canvas.view.ViewTypefaceButton;

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
        int startColor = Color.WHITE;
        int endColor = context.getResources().getColor(R.color.primary_dark);

        List<View> views = getTargets();
        View fab = views.get(0);
        ViewRoundedFrameLayout fabFrame = (ViewRoundedFrameLayout) views.get(1);
        ViewCanvasLayout layout = (ViewCanvasLayout) views.get(2);

        Point size = new Point();
        ((Activity) context).getWindowManager().getDefaultDisplay().getSize(size);
        float yDiff = layout.getHeight() - size.y;

        float xRatio = (float) fab.getWidth() / (float) fabFrame.getWidth();
        float yRatio = (float) fab.getHeight() / (float) fabFrame.getHeight();

        float translationX = fab.getX() - fab.getWidth() * .25f - fabFrame.getWidth() / 2;
        float translationY = fab.getY() + fab.getHeight() * 2.75f;

        fabFrame.setScaleX(xRatio);
        fabFrame.setScaleY(yRatio);
        fabFrame.setTranslationX(translationX);
        fabFrame.setTranslationY(translationY + yDiff);

        Animator corner = ObjectAnimator.ofFloat(fabFrame,
                ViewRoundedFrameLayout.CORNERS, fabFrame.getWidth(), 0)
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

        Animator alpha = ObjectAnimator.ofInt(layout, ViewCanvasLayout.ALPHA, 128);
        alpha.setInterpolator(new LinearInterpolator());

        Animator background = ObjectAnimator.ofArgb(fabFrame,
                ViewUtils.BACKGROUND, startColor, endColor)
                .setDuration(350);

        ArcMotion arcMotion = new ArcMotion();
        arcMotion.setMinimumVerticalAngle(70f);
        arcMotion.setMinimumHorizontalAngle(15f);
        Path motionPath = arcMotion.getPath(translationX, translationY, 0, 0);
        Animator position = ObjectAnimator.ofFloat(fabFrame,
                View.TRANSLATION_X, View.TRANSLATION_Y, motionPath)
                .setDuration(350);

        ObjectAnimator scale = ObjectAnimator.ofPropertyValuesHolder(fabFrame,
                PropertyValuesHolder.ofFloat(View.SCALE_X, fabFrame.getScaleX(), 1f),
                PropertyValuesHolder.ofFloat(View.SCALE_Y, fabFrame.getScaleX(), 1f))
                .setDuration(350);
        scale.setStartDelay(position.getDuration() / 2);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(alpha, background, corner, position, scale);
        animatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                fabFrame.setVisibility(View.VISIBLE);
                fab.setVisibility(View.GONE);

                for (int x = 0; x < layout.getChildCount(); x++) {
                    View v = layout.getChildAt(x);
                    if (!(v instanceof ViewRoundedFrameLayout)) {
                        disableView(v);
                    }
                }
            }
        });
        animatorSet.setInterpolator(new AccelerateDecelerateInterpolator());

        return animatorSet;
    }

    private void disableView(View v) {
        v.setEnabled(false);

        if (v instanceof ViewGroup) {
            ViewGroup vg = (ViewGroup) v;
            for (int i = 0; i < vg.getChildCount(); i++) {
                View child = vg.getChildAt(i);
                disableView(child);
            }
        }
    }
}
