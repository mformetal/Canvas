package milespeele.canvas.view;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPropertyAnimatorListener;
import android.support.v4.view.animation.LinearOutSlowInInterpolator;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Interpolator;
import android.widget.LinearLayout;

import butterknife.ButterKnife;
import butterknife.OnClick;
import milespeele.canvas.R;
import milespeele.canvas.fragment.FragmentDrawer;
import milespeele.canvas.util.Logger;

/**
 * Created by Miles Peele on 7/9/2015.
 */
public class ViewFabMenu extends ViewGroup
    implements View.OnClickListener {

    private static boolean isAnimatingOut = false;
    private static boolean menuVisible = true;

    private FloatingActionButton toggle;
    private LinearLayout menu;

    private final static Interpolator INTERPOLATOR = new LinearOutSlowInInterpolator();

    private float buttonMargin;
    private int buttonWidth;
    private int buttonHeight;

    private FabMenuListener mListener;
    public interface FabMenuListener {
        void onColorClicked();
        void onWidthClicked();
        void onClearClicked();
        void onUndoClicked();
        void onRedoClicked();
    }

    public ViewFabMenu(Context context) {
        super(context);
        init(context);
    }

    public ViewFabMenu(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public ViewFabMenu(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {
        buttonMargin = context.getResources().getDimension(R.dimen.fab_margin);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec){
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        for (int i = 0; i < getChildCount(); i++) {
            View v = getChildAt(i);
            v.measure(MeasureSpec.makeMeasureSpec(widthMeasureSpec, MeasureSpec.AT_MOST),
                    MeasureSpec.makeMeasureSpec(heightMeasureSpec, MeasureSpec.AT_MOST));

            buttonWidth = v.getMeasuredWidth();
            buttonHeight = v.getMeasuredHeight();
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        final int count = getChildCount();

        int left = (int) (l + getMeasuredWidth() - buttonMargin - buttonWidth);
        int right = (int) (r - buttonMargin);
        int currentBottom = (int) (b - buttonMargin);

        for (int i = count - 1; i >= 0; i--) {
            View child = getChildAt(i);
            child.layout(left, currentBottom - buttonHeight, right, currentBottom);
            currentBottom -= buttonHeight + buttonMargin;
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        ButterKnife.inject(this);
        toggle = (FloatingActionButton) getChildAt(getChildCount() - 1);
        rotateToShowMenuOpen();
    }

    @Override
    @OnClick({R.id.palette_show, R.id.palette_color, R.id.palette_brush_size,
            R.id.palette_trash, R.id.palette_undo, R.id.palette_redo})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.palette_show:
                showOrHideMenu();
                break;
            case R.id.palette_color:
                mListener.onColorClicked();
                break;
            case R.id.palette_brush_size:
                mListener.onWidthClicked();
                break;
            case R.id.palette_trash:
                mListener.onClearClicked();
                break;
            case R.id.palette_undo:
                mListener.onUndoClicked();
                break;
            case R.id.palette_redo:
                mListener.onRedoClicked();
                break;
        }
    }

    public void setListener(FabMenuListener drawer) {
        mListener = drawer;
    }

    private void showOrHideMenu() {
        if (menuVisible) {
            animateOut();
        } else {
            animateIn();
        }
    }

    public void animateOut() {
        if (menuVisible) {
            menuVisible = false;
            rotateToShowMenuClosed();
            for (int i = 0; i < getChildCount() - 1; i++) {
                View v = getChildAt(i);
                ViewCompat.animate(v)
                        .scaleY(0)
                        .alpha(0)
                        .setInterpolator(INTERPOLATOR)
                        .start();
            }
        }
    }

    private void animateIn() {
        menuVisible = true;
        rotateToShowMenuOpen();
        for (int i = 0; i < getChildCount() - 1; i++) {
            View v = getChildAt(i);
            ViewCompat.animate(v)
                    .scaleY(1)
                    .alpha(1)
                    .setInterpolator(INTERPOLATOR)
                    .start();
        }
    }

    private void rotateToShowMenuOpen() {
        ObjectAnimator imageViewObjectAnimator = ObjectAnimator.ofFloat(toggle,
                "rotation", 0f, 135f);
        imageViewObjectAnimator.start();
    }

    private void rotateToShowMenuClosed() {
        ObjectAnimator imageViewObjectAnimator = ObjectAnimator.ofFloat(toggle,
                "rotation", 135f, 270f);
        imageViewObjectAnimator.start();
    }
}
