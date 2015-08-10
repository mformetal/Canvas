package milespeele.canvas.view;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Interpolator;
import android.view.animation.OvershootInterpolator;

import java.util.List;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;
import milespeele.canvas.MainApp;
import milespeele.canvas.R;
import milespeele.canvas.event.EventColorChosen;
import milespeele.canvas.event.EventColorize;
import milespeele.canvas.event.EventErase;
import milespeele.canvas.event.EventRedo;
import milespeele.canvas.event.EventStrokeColor;
import milespeele.canvas.event.EventBrushType;
import milespeele.canvas.event.EventUndo;

/**
 * Created by milespeele on 8/7/15.
 */
public class ViewFabMenu extends ViewGroup
    implements View.OnClickListener {

    @Bind(R.id.menu_show) ViewFab toggle;
    @Bind(R.id.menu_erase) ViewFab eraser;
    @Bind({R.id.menu_colorize, R.id.menu_size, R.id.menu_stroke_color, R.id.menu_undo,
    R.id.menu_redo, R.id.menu_erase}) List<ViewFab> buttonsList;

    @Inject EventBus bus;

    private static ObjectAnimator close;
    private static ObjectAnimator open;
    private static final Interpolator INTERPOLATOR = new OvershootInterpolator();
    private Paint ripplePaint;

    private boolean isMenuShowing = true;
    private boolean isAnimating = false;
    private boolean isFirstShow = true;
    private int fabMargin;
    private float centreX, centreY;
    private static int DELAY = 20;
    private static final int DURATION = 350;
    private static final int DELAY_INCREMENT = 20;
    private final static int HALF_ALPHA = 128;
    private float radius;

    public ViewFabMenu(Context context) {
        super(context);
        init();
    }

    public ViewFabMenu(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ViewFabMenu(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public ViewFabMenu(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        ((MainApp) getContext().getApplicationContext()).getApplicationComponent().inject(this);
        bus.register(this);
        fabMargin = Math.round(getResources().getDimension(R.dimen.fab_margin));
        setDescendantFocusability(FOCUS_AFTER_DESCENDANTS);

        ripplePaint = new Paint();
        ripplePaint.setAntiAlias(true);
        ripplePaint.setStyle(Paint.Style.FILL);
        ripplePaint.setColor(getResources().getColor(R.color.accent));
        ripplePaint.setAlpha(HALF_ALPHA);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        ButterKnife.bind(this);
        close = ObjectAnimator.ofFloat(toggle, "rotation", -135f, -270f);
        open = ObjectAnimator.ofFloat(toggle, "rotation", 0f, -135f);
        open.start();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int mw = MeasureSpec.getMode(widthMeasureSpec);
        final int mh = MeasureSpec.getMode(heightMeasureSpec);
        final int sw = MeasureSpec.getSize(widthMeasureSpec);
        final int sh = MeasureSpec.getSize(heightMeasureSpec);

        final int pw = getPaddingLeft() + getPaddingRight();
        final int ph = getPaddingTop() + getPaddingBottom();

        final int s;
        final int sp;
        if (mw == MeasureSpec.UNSPECIFIED && mh == MeasureSpec.UNSPECIFIED) {
            throw new IllegalArgumentException("Layout must be constrained on at least one axis");
        } else if (mw == MeasureSpec.UNSPECIFIED) {
            s = sh;
            sp = s - ph;
        } else if (mh == MeasureSpec.UNSPECIFIED) {
            s = sw;
            sp = s - pw;
        } else {
            if (sw - pw < sh - ph) {
                s = sw;
                sp = s - pw;
            } else {
                s = sh;
                sp = s - ph;
            }
        }

        final int spp = Math.max(sp, 0);

        final int size = 5;
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                final View child = getChildAt(y * size + x);
                if (child == null) continue;
                measureChildWithMargins(child,
                        MeasureSpec.makeMeasureSpec((spp + x) / size, MeasureSpec.EXACTLY), 0,
                        MeasureSpec.makeMeasureSpec((spp) / size, MeasureSpec.EXACTLY), 0
                );
            }
        }

        int dimen = mw == MeasureSpec.EXACTLY ? sw : sp + pw;
        setMeasuredDimension(dimen, dimen / 4);
    }

    @Override
    protected LayoutParams generateDefaultLayoutParams() {
        return new MarginLayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
    }

    @Override
    protected LayoutParams generateLayoutParams(LayoutParams p) {
        return new MarginLayoutParams(p);
    }

    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new MarginLayoutParams(getContext(), attrs);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        final int count = getChildCount();

        toggle.layout(r / 2 - toggle.getMeasuredWidth() / 2,
                getMeasuredHeight() - toggle.getMeasuredHeight() - fabMargin,
                r / 2 + toggle.getMeasuredWidth() / 2,
                getMeasuredHeight() - fabMargin);

        final int radius = toggle.getMeasuredHeight() * 2;
        centreX = toggle.getX() + toggle.getWidth()  / 2;
        centreY = toggle.getY() + toggle.getHeight() / 2;
        final double slice = Math.PI / (count - 2);
        for (int i = count - 2; i >= 0; i--) {
            final View child = getChildAt(i);

            double angle = -(slice * (i));
            double x = centreX + radius * Math.cos(angle);
            double y = centreY + radius * Math.sin(angle);

            child.layout((int) x - child.getMeasuredWidth() / 2,
                    (int) y - child.getMeasuredHeight() / 2,
                    (int) x + child.getMeasuredWidth() / 2,
                    (int) y + child.getMeasuredHeight() / 2);
        }
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        if (isFirstShow) {
            canvas.drawCircle(canvas.getWidth() / 2, canvas.getHeight(),
                    (radius = getMeasuredHeight()), ripplePaint);
        } else {
            canvas.drawCircle(canvas.getWidth() / 2, canvas.getHeight(), radius, ripplePaint);
        }
        super.dispatchDraw(canvas);
    }

    @Override
    @OnClick({R.id.menu_colorize, R.id.menu_size, R.id.menu_stroke_color, R.id.menu_undo,
        R.id.menu_redo, R.id.menu_erase, R.id.menu_show})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.menu_show:
                toggleMenu();
                break;
            case R.id.menu_colorize:
                eraser.scaleDown();
                bus.post(new EventColorize());
                break;
            case R.id.menu_size:
                eraser.scaleDown();
                ViewCanvasLayout parent = ((ViewCanvasLayout) getParent());
                bus.post(new EventBrushType(parent.getBrushWidth(), parent.getPaintAlpha()));
                break;
            case R.id.menu_stroke_color:
                eraser.scaleDown();
                bus.post(new EventStrokeColor());
                break;
            case R.id.menu_undo:
                bus.post(new EventUndo());
                break;
            case R.id.menu_redo:
                bus.post(new EventRedo());
                break;
            case R.id.menu_erase:
                eraser.toggleScaled();
                bus.post(new EventErase());
                break;
        }
    }

    public void toggleMenu() {
        if (!isAnimating) {
            if (isMenuShowing) {
                hide();
            } else {
                show();
            }
        }
    }

    public void show() {
        if (!isMenuShowing && !isAnimating) {
            animateReveal();
            isAnimating = true;
            isMenuShowing = true;
            open.start();

            int delay = DELAY;
            for (ViewFab view: buttonsList) {
                float diffX = view.getX() - centreX, diffY = view.getY() - centreY;

                AnimatorSet out = new AnimatorSet();
                out.playTogether(ObjectAnimator.ofFloat(view, "translationX", diffX),
                        ObjectAnimator.ofFloat(view, "translationY", diffY),
                        ObjectAnimator.ofFloat(view, "alpha", 0.0f, 1.0f));
                out.setStartDelay(delay);
                out.setInterpolator(INTERPOLATOR);
                out.addListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        view.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        if (view == buttonsList.get(buttonsList.size() - 1)) {
                            isAnimating = false;
                        }
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                });
                out.start();
                delay += DELAY_INCREMENT;
            }
        }
    }

    public void hide() {
        if (isMenuShowing && !isAnimating) {
            isFirstShow = false;
            dismissReveal();
            isAnimating = true;
            isMenuShowing = false;
            close.start();

            int delay = DELAY;
            for (ViewFab view: buttonsList) {
                float diffX = view.getX() - centreX, diffY = view.getY() - centreY;

                AnimatorSet out = new AnimatorSet();
                out.playTogether(ObjectAnimator.ofFloat(view, "translationY", -diffY),
                        ObjectAnimator.ofFloat(view, "translationX", -diffX),
                        ObjectAnimator.ofFloat(view, "alpha", 1.0f, 0.0f));
                out.setStartDelay(delay);
                out.setInterpolator(INTERPOLATOR);
                out.setDuration(DURATION);
                out.addListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        view.setVisibility(View.GONE);
                        if (view == buttonsList.get(buttonsList.size() - 1)) {
                            isAnimating = false;
                        }
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                });
                out.start();
                delay += DELAY_INCREMENT;
            }
        }
    }

    private float getRadius() {
        return radius;
    }

    public void setRadius(float radius) {
        this.radius = radius;
        invalidate();
    }

    public void animateReveal() {
        ObjectAnimator radius = ObjectAnimator.ofFloat(this, "radius", getMeasuredHeight());
        radius.setDuration(Math.round(DURATION * 1.5));
        radius.setInterpolator(INTERPOLATOR);
        radius.start();
        invalidate();
    }

    public void dismissReveal() {
        ObjectAnimator radius = ObjectAnimator.ofFloat(this, "radius", getMeasuredHeight(), 0);
        radius.setDuration(Math.round(DURATION * 1.5));
        radius.setInterpolator(INTERPOLATOR);
        radius.setStartDelay(DELAY * 8);
        radius.start();
        invalidate();
    }

    public void onEvent(EventColorChosen eventColorChosen) {
        if (eventColorChosen.color != 0) {
            if (eventColorChosen.which.equals(getResources().getString(R.string.TAG_FRAGMENT_FILL))) {
                eraser.scaleDown();
            } else {
                eraser.scaleDown();
            }
        }
    }
}