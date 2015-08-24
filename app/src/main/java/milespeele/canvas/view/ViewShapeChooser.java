package milespeele.canvas.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by Miles Peele on 8/17/2015.
 */
public class ViewShapeChooser extends ViewGroup {

    private int mSize = 5;
    private int mSquareDimensions = 1;

    public ViewShapeChooser(Context context) {
        super(context);
        init();
    }

    public ViewShapeChooser(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ViewShapeChooser(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public ViewShapeChooser(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {

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
                        l + (s *  x   ) / size + lps.leftMargin,
                        t + (s *  y   ) / size,
                        l + (s * (x+1)) / size - lps.rightMargin,
                        t + (s * (y+1)) / size);
            }
        }
    }
}
