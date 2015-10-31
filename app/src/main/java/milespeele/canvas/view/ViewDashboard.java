package milespeele.canvas.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Rect;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import milespeele.canvas.MainApp;
import milespeele.canvas.R;
import milespeele.canvas.util.Datastore;
import milespeele.canvas.util.Logg;

/**
 * Created by mbpeele on 10/19/15.
 */
public class ViewDashboard extends ViewGroup implements View.OnClickListener {

    @Bind({R.id.dashboard_draw, R.id.dashboard_import, R.id.dashboard_profile,
            R.id.dashboard_social}) List<ViewDashboardButton> buttons;

    @Inject Datastore store;

    private Rect rect = new Rect();

    private ViewDashboardListener listener;
    public interface ViewDashboardListener {
        void onDashboardButtonClicked(int buttonId);
    }

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
        ((MainApp) getContext().getApplicationContext()).getApplicationComponent().inject(this);

        setWillNotDraw(false);
        setSaveEnabled(true);
        setWillNotCacheDrawing(false);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        ButterKnife.bind(this);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        int x = (int) ev.getX(), y = (int) ev.getY();
        ViewDashboardButton draw = buttons.get(0);
        draw.getHitRect(rect);
        if (rect.contains(x, y)) {
            store.putTouchRevealCoordinates(ev.getX(), ev.getY());
        }
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    @OnClick({R.id.dashboard_draw, R.id.dashboard_import, R.id.dashboard_profile,
            R.id.dashboard_social})
    public void onClick(View v) {
        if (listener != null) {
            listener.onDashboardButtonClicked(v.getId());
        }
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
        final float children = (float) getChildCount();

        int curTop = b / 8;

        int slice;
        if (children % 2 == 0) {
            slice = Math.round(((b - curTop) * (1 / (children / 2))));
        } else {
            slice = Math.round((b - curTop) * (1 / ((children + 1) / 2)));
        }

        for (int x = 0; x < children; x++) {
            View child = getChildAt(x);

            if (x % 2 == 0) {
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

    public void setListener(ViewDashboardListener otherListener) {
        listener = otherListener;
    }
}
