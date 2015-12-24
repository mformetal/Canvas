package milespeele.canvas.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Region;
import android.support.design.widget.CoordinatorLayout;
import android.util.AttributeSet;
import android.util.Property;
import android.view.View;
import android.widget.FrameLayout;

import milespeele.canvas.R;
import milespeele.canvas.util.Logg;
import milespeele.canvas.util.ViewUtils;

/**
 * Created by mbpeele on 12/23/15.
 */
public class ViewRoundedFrameLayout extends FrameLayout {

    private Path mPath;
    private RectF mRect;

    private float mCorner;

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

        mRect = new RectF();

        setWillNotDraw(false);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mRect.set(0, 0, w, h);
        mPath.addRoundRect(mRect, mCorner, mCorner, Path.Direction.CW);
    }

    @Override
    public void draw(Canvas canvas) {
        final int count = canvas.save();
        mPath.reset();
        mPath.addRoundRect(mRect, mCorner, mCorner, Path.Direction.CW);
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
