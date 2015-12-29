package milespeele.canvas.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import milespeele.canvas.R;
import milespeele.canvas.util.Logg;
import milespeele.canvas.util.ViewUtils;

/**
 * Created by mbpeele on 12/23/15.
 */
public class ViewOptionsMenu extends ViewGroup implements View.OnClickListener {

    @Bind(R.id.view_options_menu_cancel) ViewFab cancel;
    @Bind(R.id.view_options_menu_accept) ViewFab accept;

    private ArrayList<ViewOptionsMenuListener> listeners;
    public interface ViewOptionsMenuListener {
        void onOptionsMenuCancel();
        void onOptionsMenuButtonClicked(View view);
        void onOptionsMenuAccept();
    }

    public ViewOptionsMenu(Context context) {
        super(context);
        init();
    }

    public ViewOptionsMenu(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ViewOptionsMenu(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public ViewOptionsMenu(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        listeners = new ArrayList<>();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            measureChildWithMargins(child, widthMeasureSpec, 0, heightMeasureSpec, 0);
        }

        int width = getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec);

        setMeasuredDimension(width, getSuggestedMinimumHeight());
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
        if (!changed) {
            return;
        }

        MarginLayoutParams lps = (MarginLayoutParams) cancel.getLayoutParams();

        final int count = getChildCount();
        int viewRadius = cancel.getMeasuredWidth() / 2;
        float middleX = (l + r) / 2f;
        float middleY = getMeasuredHeight() - lps.bottomMargin - cancel.getMeasuredHeight();
        float itemRadius = cancel.getMeasuredHeight() * 3;

        for (int i = 0; i < count; i++) {
            View view = getChildAt(i);

            double angle = Math.toRadians(180d * i / (count - 1));
            if (i == 1) {
                angle = Math.toRadians(70);
            } else if (i == 2) {
                angle = Math.toRadians(110d);
            }

            double x = middleX + itemRadius * Math.cos(angle);
            double y = middleY;

            view.layout((int) x - viewRadius, (int) y - viewRadius,
                    (int) x + viewRadius, (int) y + viewRadius);
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        ButterKnife.bind(this);
    }

    @Override
    @OnClick({R.id.view_options_menu_cancel, R.id.view_options_menu_accept,
            R.id.view_options_menu_1})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.view_options_menu_cancel:
                for (ViewOptionsMenuListener listener : listeners) {
                    listener.onOptionsMenuCancel();
                }
                break;
            case R.id.view_options_menu_accept:
                for (ViewOptionsMenuListener listener : listeners) {
                    listener.onOptionsMenuAccept();
                }
                break;
            case R.id.view_options_menu_1:
            case R.id.view_options_menu_2:
                for (ViewOptionsMenuListener listener : listeners) {
                    listener.onOptionsMenuButtonClicked(v);
                }
                break;
        }
    }

    public void addListener(ViewOptionsMenuListener optionsMenuListener) {
        listeners.add(optionsMenuListener);
    }
}