package milespeele.canvas.view;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.TypefaceSpan;
import android.util.AttributeSet;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;

import milespeele.canvas.R;
import milespeele.canvas.util.Logg;
import milespeele.canvas.util.TypefaceSpanner;

/**
 * Created by mbpeele on 10/27/15.
 */
public class ViewToolbar extends Toolbar {

    private ObjectAnimator animator;
    private static final Interpolator INTERPOLATOR = new AccelerateDecelerateInterpolator();

    public ViewToolbar(Context context) {
        super(context);
        init();
    }

    public ViewToolbar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ViewToolbar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        String text = getContext().getString(R.string.app_name);
        SpannableString s = new SpannableString(text);
        s.setSpan(new TypefaceSpanner(getContext(), "Roboto.ttf"), 0, s.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        setTitle(s);
    }

    @Override
    public void setElevation(float elevation) {
        super.setElevation(4f);
    }

    public void animateOut() {
        animator = ObjectAnimator.ofFloat(this, "translationY", -getHeight());
        animator.setDuration(750);
        animator.setInterpolator(INTERPOLATOR);
        animator.start();
    }

    public void animateIn() {
        animator = ObjectAnimator.ofFloat(this, "translationY", 0);
        animator.setStartDelay(350);
        animator.setDuration(500);
        animator.setInterpolator(INTERPOLATOR);
        animator.start();
    }
}
