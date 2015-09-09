package milespeele.canvas.view;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnticipateInterpolator;
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
import milespeele.canvas.event.EventFilenameChosen;
import milespeele.canvas.event.EventParseError;
import milespeele.canvas.event.EventShowBrushPicker;
import milespeele.canvas.event.EventColorChosen;
import milespeele.canvas.event.EventShowColorize;
import milespeele.canvas.event.EventShowErase;
import milespeele.canvas.event.EventShowCanvasColorPicker;
import milespeele.canvas.event.EventRedo;
import milespeele.canvas.event.EventShowFilenameFragment;
import milespeele.canvas.event.EventShowShapeChooser;
import milespeele.canvas.event.EventShowStrokePickerColor;
import milespeele.canvas.event.EventUndo;
import milespeele.canvas.util.AbstractAnimatorListener;

/**
 * Created by milespeele on 8/7/15.
 */
public class ViewFabMenu extends ViewGroup
    implements View.OnClickListener {

    @Bind(R.id.menu_show) ViewFab toggle;
    @Bind(R.id.menu_erase) ViewFab eraser;
    @Bind(R.id.menu_save) ViewFab saver;
    @Bind({R.id.menu_shape_chooser, R.id.menu_colorize,  R.id.menu_stroke_color, R.id.menu_size,
            R.id.menu_undo, R.id.menu_redo, R.id.menu_erase, R.id.menu_new_canvas, R.id.menu_save})
    List<ViewFab> buttonsList;

    @Inject EventBus bus;

    private static ObjectAnimator close;
    private static ObjectAnimator open;
    private static final Interpolator OVERSHOOT_INTERPOLATOR = new OvershootInterpolator();
    private static final Interpolator ANTICIPATE_INTERPOLATOR = new AnticipateInterpolator();

    private boolean isMenuShowing = true;
    private boolean isAnimating = false;
    private boolean isMenuGone = false;
    private float centreX, centreY;
    private final static int VISIBILITY_DURATION = 350;
    private final static int DELAY = 0;
    private final static int DURATION = 400;
    private final static int DELAY_INCREMENT = 15;

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
        setDescendantFocusability(FOCUS_AFTER_DESCENDANTS);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        ButterKnife.bind(this);
        close = ObjectAnimator.ofFloat(toggle, "rotation", -135f, -270f);
        close.setDuration(Math.round(DURATION * 1.5));
        open = ObjectAnimator.ofFloat(toggle, "rotation", 0f, -135f);
        open.start();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        measureChildWithMargins(toggle, widthMeasureSpec, 0, heightMeasureSpec, 0);

        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            if (child == toggle) continue;
            measureChildWithMargins(child, widthMeasureSpec, 0, heightMeasureSpec, 0);
        }

        int width = getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec);

        setMeasuredDimension(width, width);
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

        MarginLayoutParams lps = (MarginLayoutParams) toggle.getLayoutParams();

        toggle.layout(r / 2 - toggle.getMeasuredWidth() / 2,
                getMeasuredHeight() - toggle.getMeasuredHeight() - lps.bottomMargin,
                r / 2 + toggle.getMeasuredWidth() / 2,
                getMeasuredHeight() - lps.bottomMargin);

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
    @OnClick({R.id.menu_colorize, R.id.menu_size, R.id.menu_stroke_color, R.id.menu_undo,
        R.id.menu_redo, R.id.menu_erase, R.id.menu_show, R.id.menu_new_canvas, R.id.menu_save,
        R.id.menu_shape_chooser})
    public void onClick(View v) {
        ViewCanvasLayout parent = ((ViewCanvasLayout) getParent());
        switch (v.getId()) {
            case R.id.menu_show:
                toggleMenu();
                break;
            case R.id.menu_colorize:
                eraser.scaleDown();
                bus.post(new EventShowColorize());
                break;
            case R.id.menu_size:
                eraser.scaleDown();
                bus.post(new EventShowBrushPicker(parent.getBrushWidth()));
                break;
            case R.id.menu_stroke_color:
                eraser.scaleDown();
                bus.post(new EventShowStrokePickerColor(parent.getDrawerColor()));
                break;
            case R.id.menu_undo:
                bus.post(new EventUndo());
                break;
            case R.id.menu_redo:
                bus.post(new EventRedo());
                break;
            case R.id.menu_erase:
                eraser.toggleScaled();
                bus.post(new EventShowErase());
                break;
            case R.id.menu_new_canvas:
                eraser.scaleDown();
                bus.post(new EventShowCanvasColorPicker());
                break;
            case R.id.menu_save:
                bus.post(new EventShowFilenameFragment());
                break;
            case R.id.menu_shape_chooser:
                bus.post(new EventShowShapeChooser());
                break;
        }
    }

    public void toggleMenu() {
        if (!isAnimating) {
            if (isMenuGone) {
                setVisibilityVisible();
            } else {
                if (isMenuShowing) {
                    hide();
                } else {
                    show();
                }
            }
        }
    }

    public void show() {
        if (!isMenuShowing && !isAnimating) {
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
                out.setDuration(DURATION);
                out.setInterpolator(OVERSHOOT_INTERPOLATOR);
                out.addListener(new AbstractAnimatorListener() {
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
                });
                out.start();
                delay += DELAY_INCREMENT;
            }
        }
    }

    public void hide() {
        if (isMenuShowing && !isAnimating) {
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
                out.setDuration(DURATION);
                out.setInterpolator(ANTICIPATE_INTERPOLATOR);
                out.addListener(new AbstractAnimatorListener() {

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        view.setVisibility(View.GONE);
                        if (view == buttonsList.get(buttonsList.size() - 1)) {
                            isAnimating = false;
                        }
                    }
                });
                out.start();
                delay += DELAY_INCREMENT;
            }
        }
    }

    public void setVisibilityGone() {
        if (!isMenuGone && isMenuShowing) {
            isMenuGone = true;
            close.start();
            ButterKnife.apply(buttonsList, GONE);
        }
    }

    public void setVisibilityVisible() {
        if (isMenuGone) {
            isMenuGone = false;
            open.start();
            ButterKnife.apply(buttonsList, VISIBLE);
        }
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

    public void onEvent(EventFilenameChosen eventFilenameChosen) {
        saver.startPulse();
    }

    public void onEvent(EventParseError eventParseError) {
        saver.stopPulse();
    }

    static final ButterKnife.Action<View> GONE = new ButterKnife.Action<View>() {

        @Override
        public void apply(View view, int index) {
            ObjectAnimator gone = ObjectAnimator.ofFloat(view, "alpha", 1f, 0f);
            gone.setDuration(VISIBILITY_DURATION);
            gone.addListener(new AbstractAnimatorListener() {

                @Override
                public void onAnimationEnd(Animator animation) {
                    view.setVisibility(View.GONE);
                }
            });
            gone.start();
        }
    };

    static final ButterKnife.Action<View> VISIBLE = new ButterKnife.Action<View>() {
        @Override
        public void apply(View view, int index) {
            ObjectAnimator gone = ObjectAnimator.ofFloat(view, "alpha", 0f, 1f);
            gone.setDuration(VISIBILITY_DURATION);
            gone.addListener(new AbstractAnimatorListener() {

                @Override
                public void onAnimationStart(Animator animation) {
                    view.setVisibility(View.VISIBLE);
                }
            });
            gone.start();
        }
    };
}