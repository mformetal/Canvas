package milespeele.canvas.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Region;
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
    private Paint mPaint;
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

        mPaint = new Paint();
//        mPaint.setColor(getResources().getColor(R.color.primary_dark));
        mPaint.setColor(Color.YELLOW);

        mRect = new RectF();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
//        mRect.set(0, 0, w, h);
//        float halfWidth = w / 2f;
//        float halfHeight = h / 2f;
//        mPath.reset();
//        mPath.addCircle(halfWidth, halfHeight, Math.min(halfWidth, halfHeight), Path.Direction.CW);
//        mPath.close();
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        Path path = new Path();
        path.addCircle(canvas.getWidth() / 2f, canvas.getHeight() / 2f,
                Math.min(canvas.getWidth() / 2f, canvas.getHeight() / 2f), Path.Direction.CW);
        int count = canvas.save();
////        Logg.log(mCorner);
////        mPath.addRoundRect(mRect, mCorner, mCorner, Path.Direction.CW);
        canvas.clipPath(path);
        super.dispatchDraw(canvas);
        canvas.restoreToCount(count);
    }

    @Override
    protected boolean drawChild(Canvas canvas, View child, long drawingTime) {
        Path path = new Path();
        path.addCircle(canvas.getWidth() / 2f, canvas.getHeight() / 2f,
                Math.min(canvas.getWidth() / 2f, canvas.getHeight() / 2f), Path.Direction.CW);
        int count = canvas.save();
        canvas.clipPath(path);
        return super.drawChild(canvas, child, drawingTime);
    }

    public float getCorner() {
        return mCorner;
    }

    public void setCorner(float corner) {
        mCorner = corner;
        invalidate();
    }

//    @Override
//    public void setScaleX(float scaleX) {
//        super.setScaleX(scaleX);
//        if (mRect != null) {
//
//        }
//    }
//
//    @Override
//    public void setScaleY(float scaleY) {
//        super.setScaleY(scaleY);
//        if (mRect != null) {
//
//        }
//    }

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
