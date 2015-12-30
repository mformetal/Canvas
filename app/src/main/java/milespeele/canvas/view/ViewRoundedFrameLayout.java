package milespeele.canvas.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Outline;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Region;
import android.support.design.widget.CoordinatorLayout;
import android.util.AttributeSet;
import android.util.Property;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.widget.FrameLayout;

import milespeele.canvas.R;
import milespeele.canvas.util.Logg;
import milespeele.canvas.util.ViewUtils;

/**
 * Created by mbpeele on 12/23/15.
 */
public class ViewRoundedFrameLayout extends FrameLayout {

    private Path mPath;

    private float mCorner;
    private boolean isAnimating = false;

    public ViewRoundedFrameLayout(Context context) {
        super(context);
        init();
    }

    public ViewRoundedFrameLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ViewRoundedFrameLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public ViewRoundedFrameLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
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

    public static final ViewUtils.FloatProperty<ViewRoundedFrameLayout> CORNERS
            = new ViewUtils.FloatProperty<ViewRoundedFrameLayout>("corners") {

        @Override
        public Float get(ViewRoundedFrameLayout object) {
            return object.getCorner();
        }

        @Override
        public void setValue(ViewRoundedFrameLayout object, float value) {
            object.setCorner(value);
        }
    };
}
