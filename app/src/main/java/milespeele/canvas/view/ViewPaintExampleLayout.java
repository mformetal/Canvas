package milespeele.canvas.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Build;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewParent;
import android.widget.LinearLayout;

import java.util.Random;

import butterknife.Bind;
import butterknife.ButterKnife;
import milespeele.canvas.R;
import milespeele.canvas.paint.PaintStyles;
import milespeele.canvas.util.Logg;

/**
 * Created by Miles Peele on 9/6/2015.
 */
public class ViewPaintExampleLayout extends LinearLayout implements View.OnClickListener {

    @Bind(R.id.paint_example_layout_text) ViewTypefaceTextView typefaceTextView;
    @Bind(R.id.paint_example_layout_paint) ViewPaintExample paintExample;

    private String which;
    private Paint examplePaint;

    private boolean isAnimated = false;

    public ViewPaintExampleLayout(Context context) {
        super(context);
        init(context, null);
    }

    public ViewPaintExampleLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public ViewPaintExampleLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public ViewPaintExampleLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        LayoutInflater.from(context).inflate(R.layout.paint_example_layout, this, true);

        setClipChildren(false);

        TypedArray typed = context.obtainStyledAttributes(attrs, R.styleable.ViewPaintExampleLayout);
        which = typed.getString(R.styleable.ViewPaintExampleLayout_paintType);
        typed.recycle();

        examplePaint = PaintStyles.getStyleFromAttrs(which, Color.WHITE, getContext());

        setOnClickListener(this);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        ButterKnife.bind(this);

        typefaceTextView.setText(which);
        paintExample.setPaint(examplePaint);
    }

    public void changeExamplePaintViewThickness(float thickness) {
        paintExample.onThicknessChanged(thickness);
    }

    public void dehighlight() {
        if (isAnimated) {
            typefaceTextView.animateTextColorChange(getResources().getColor(R.color.primary_text), Color.WHITE);
            isAnimated = false;
        }
    }

    @Override
    public void onClick(View v) {
        typefaceTextView.animateTextColorChange(Color.WHITE, getResources().getColor(R.color.spirit_gold));
        ((ViewBrushPickerLayout) getParent()).changeExamplePaint(paintExample.getExamplePaint());
        isAnimated = true;
    }
}
