package milespeele.canvas.view;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Build;
import android.text.TextPaint;
import android.transition.Fade;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import javax.inject.Inject;

import de.greenrobot.event.EventBus;
import milespeele.canvas.MainApp;
import milespeele.canvas.R;
import milespeele.canvas.activity.ActivityHome;
import milespeele.canvas.event.EventDashboardButtonClicked;
import milespeele.canvas.util.Logg;

/**
 * Created by mbpeele on 10/19/15.
 */
public class ViewDashboard extends ViewGroup {

    private Rect bounds;

    public ViewDashboard(Context context) {
        super(context);
        init();
    }

    public ViewDashboard(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ViewDashboard(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public ViewDashboard(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        bounds = new Rect();

        setWillNotDraw(false);
        setSaveEnabled(true);
        setWillNotCacheDrawing(false);
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
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            measureChildWithMargins(child, widthMeasureSpec, 0, heightMeasureSpec, 0);
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        final float children = (float) getChildCount() - 1;

        ViewTypefaceTextView title = (ViewTypefaceTextView) getChildAt(0);
        TextPaint titlePaint = title.getPaint();
        titlePaint.getTextBounds("Py", 0, 2, bounds);
        title.layout(l, t, r, (int) (bounds.height() * ((double) Math.max(r, b) / (double) Math.min(r, b))));

        int curTop = title.getHeight();
        int slice;
        if (children % 2 == 0) {
            slice = Math.round((b - curTop) * (1 / (children / 2)));
        } else {
            slice = Math.round((b - curTop) * (1 / ((children + 1) / 2)));
        }

        for (int x = 1; x <= children; x++) {
            View child = getChildAt(x);
            if (x % 2 != 0) {
                child.layout(
                        l,
                        curTop,
                        r / 2,
                        curTop + slice);
            } else {
                child.layout(
                        r / 2,
                        curTop,
                        r,
                        curTop + slice);
                curTop += slice;
            }
        }
    }
}
