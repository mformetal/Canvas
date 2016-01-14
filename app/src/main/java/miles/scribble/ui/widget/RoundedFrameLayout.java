package miles.scribble.ui.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.Region;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import miles.scribble.util.ViewUtils;

/**
 * Created by mbpeele on 12/23/15.
 */
public class RoundedFrameLayout extends FrameLayout {

    private Path mPath;

    private float mCorner;
    private boolean isAnimating = false;

    public RoundedFrameLayout(Context context) {
        super(context);
        init();
    }

    public RoundedFrameLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public RoundedFrameLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public RoundedFrameLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        mPath = new Path();
    }

    @Override
    public void draw(Canvas canvas) {
        Rect rect = canvas.getClipBounds();
        final int count = canvas.save();
        mPath.reset();
        mPath.addRoundRect(rect.left, rect.top, rect.right, rect.bottom,
                mCorner, mCorner, Path.Direction.CW);
        canvas.clipPath(mPath, Region.Op.INTERSECT);
        super.draw(canvas);
        canvas.restoreToCount(count);
    }

    public float getCorner() {
        return mCorner;
    }

    public void setCorner(float corner) {
        mCorner = corner;
        invalidate();
    }

    public void setAnimating(boolean bool) {
        isAnimating = bool;
    }

    public boolean isAnimating() {
        return isAnimating;
    }

    public static final ViewUtils.FloatProperty<RoundedFrameLayout> CORNERS
            = new ViewUtils.FloatProperty<RoundedFrameLayout>("corners") {

        @Override
        public Float get(RoundedFrameLayout object) {
            return object.getCorner();
        }

        @Override
        public void setValue(RoundedFrameLayout object, float value) {
            object.setCorner(value);
        }
    };
}
