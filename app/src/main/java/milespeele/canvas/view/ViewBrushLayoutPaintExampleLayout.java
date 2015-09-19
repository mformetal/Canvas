package milespeele.canvas.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;

import butterknife.Bind;
import butterknife.ButterKnife;
import milespeele.canvas.R;
import milespeele.canvas.util.Logg;

/**
 * Created by Miles Peele on 9/6/2015.
 */
public class ViewBrushLayoutPaintExampleLayout extends LinearLayout {

    @Bind(R.id.paint_example_layout_text) ViewTypefaceTextView typefaceTextView;
    @Bind(R.id.paint_example_layout_paint) ViewBrushLayoutPaintExample paintExample;

    private boolean hasColorChangedToGold = false;

    public ViewBrushLayoutPaintExampleLayout(Context context) {
        super(context);
        init(context, null);
    }

    public ViewBrushLayoutPaintExampleLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public ViewBrushLayoutPaintExampleLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public ViewBrushLayoutPaintExampleLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {

    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        ButterKnife.bind(this);
    }

    public Paint getPaintFromExample() {
        return paintExample.getExamplePaint();
    }

    public boolean isHasColorChangedToGold() {
        return hasColorChangedToGold;
    }

    public void changeExamplePaintViewThickness(float thickness) {
        paintExample.onThicknessChanged(thickness);
    }

    public void dehighlight() {
        if (hasColorChangedToGold) {
            typefaceTextView.setTextColor(Color.WHITE);
            hasColorChangedToGold = false;
        }
    }

    public void highlight() {
        if (!hasColorChangedToGold) {
            typefaceTextView.setTextColor(getResources().getColor(R.color.spirit_gold));
            hasColorChangedToGold = true;
        }
    }
}
