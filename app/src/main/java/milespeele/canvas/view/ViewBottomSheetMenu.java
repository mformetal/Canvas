package milespeele.canvas.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import milespeele.canvas.R;
import milespeele.canvas.fragment.FragmentDrawer;

/**
 * Created by milespeele on 8/1/15.
 */
public class ViewBottomSheetMenu extends ViewGroup implements View.OnClickListener {

    private int mSize = 5;
    private int mSquareDimensions = 1;

    @InjectView(R.id.fab_menu_erase) ViewBottomSheetMenuButton eraseButton;
    @InjectView(R.id.fab_menu_colorize) ViewBottomSheetMenuButton colorizeButton;

    private FabMenuListener mListener;
    public interface FabMenuListener {
        void onColorizeClicked();
        void onEraseClicked();
        void onPaintColorClicked(int viewId);
        void onBrushClicked();
        void onUndoClicked();
        void onRedoClicked();
    }

    public ViewBottomSheetMenu(Context context) {
        super(context);
        init();
    }

    public ViewBottomSheetMenu(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ViewBottomSheetMenu(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {

    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        ButterKnife.inject(this);
    }

    public void setListener(FragmentDrawer drawer) {
        mListener = drawer;
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

        final int size = mSize;
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

        setMeasuredDimension(
                mw == MeasureSpec.EXACTLY ? sw : sp + pw,
                mh == MeasureSpec.EXACTLY ? sh : sp + ph);
        mSquareDimensions = sp;
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
        final int s = mSquareDimensions;

        final int size = mSize;
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                View child = getChildAt(y * mSize + x);
                if (child == null) return;
                MarginLayoutParams lps = (MarginLayoutParams) child.getLayoutParams();
                child.layout(
                        l + (s *  x) / size + lps.leftMargin,
                        t + (s *  y) / size,
                        l + (s * (x + 1)) / size - lps.rightMargin,
                        t + (s * (y + 1)) / size);
            }
        }
    }

    @Override
    @OnClick({R.id.fab_menu_stroke_color, R.id.fab_menu_brush,
            R.id.fab_menu_undo, R.id.fab_menu_redo, R.id.fab_menu_erase, R.id.fab_menu_colorize})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fab_menu_colorize:
                eraseButton.setSelected(false);
                mListener.onColorizeClicked();
                break;
            case R.id.fab_menu_erase:
                eraseButton.toggleSelected();
                mListener.onEraseClicked();
                break;
            case R.id.fab_menu_stroke_color:
                eraseButton.setSelected(false);
                mListener.onPaintColorClicked(v.getId());
                break;
            case R.id.fab_menu_brush:
                mListener.onBrushClicked();
                break;
            case R.id.fab_menu_undo:
                mListener.onUndoClicked();
                break;
            case R.id.fab_menu_redo:
                mListener.onRedoClicked();
                break;
        }
    }
}
