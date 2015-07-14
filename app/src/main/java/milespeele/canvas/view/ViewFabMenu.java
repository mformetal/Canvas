package milespeele.canvas.view;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.animation.LinearOutSlowInInterpolator;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Interpolator;

import butterknife.ButterKnife;
import butterknife.OnClick;
import milespeele.canvas.R;

/**
 * Created by Miles Peele on 7/9/2015.
 */
public class ViewFabMenu extends ViewGroup
        implements View.OnClickListener {

    private boolean menuVisible = true;

    private ViewFab toggle;

    private final static Interpolator INTERPOLATOR = new LinearOutSlowInInterpolator();

    private float buttonMargin;
    private int buttonWidth;
    private int buttonHeight;

    private FabMenuListener mListener;
    public interface FabMenuListener {
        void onShapeClicked();
        void onPaintColorClicked(int viewId);
        void onBrushClicked();
        void onUndoClicked();
        void onRedoClicked();
        void onFillClicked(int viewId);
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
        buttonMargin = getResources().getDimension(R.dimen.fab_margin);
    }

    @Override
    public MarginLayoutParams generateLayoutParams(AttributeSet attrs) {
        return new MarginLayoutParams(getContext(), attrs);
    }

    @Override
    protected MarginLayoutParams generateLayoutParams(LayoutParams p) {
        return new MarginLayoutParams(p);
    }

    @Override
    protected MarginLayoutParams generateDefaultLayoutParams() {
        return new MarginLayoutParams(MarginLayoutParams.WRAP_CONTENT,
                MarginLayoutParams.MATCH_PARENT);
    }

    @Override
    protected boolean checkLayoutParams(LayoutParams p) {
        return p instanceof MarginLayoutParams;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec){
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        final int count = getChildCount();

        int maxHeight = 0;
        for (int i = 0; i < count; i++) {
            View v = getChildAt(i);
            v.measure(MeasureSpec.makeMeasureSpec(widthMeasureSpec, MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec(heightMeasureSpec, MeasureSpec.EXACTLY));

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
        toggle = (ViewFab) getChildAt(getChildCount() - 1);
        rotateToShowMenuOpen();
    }

    @Override
    @OnClick({R.id.palette_show, R.id.palette_paint, R.id.palette_brush_size,
            R.id.palette_undo, R.id.palette_redo, R.id.palette_fill_canvas, R.id.palette_shape})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.palette_shape:
                mListener.onShapeClicked();
            case R.id.palette_fill_canvas:
                mListener.onFillClicked(v.getId());
                break;
            case R.id.palette_show:
                showOrHideMenu();
                break;
            case R.id.palette_paint:
                mListener.onPaintColorClicked(v.getId());
                break;
            case R.id.palette_brush_size:
                mListener.onBrushClicked();
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