package milespeele.canvas.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Build;
import android.util.AttributeSet;
import android.view.LayoutInflater;
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
public class ViewPaintExampleLayout extends LinearLayout {

    @Bind(R.id.paint_example_layout_text) ViewTypefaceTextView typefaceTextView;
    @Bind(R.id.paint_example_layout_paint) ViewPaintExample paintExample;

    private String which;
    private Paint examplePaint;

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

        TypedArray typed = context.obtainStyledAttributes(attrs, R.styleable.ViewPaintExampleLayout);
        which = typed.getString(R.styleable.ViewPaintExampleLayout_paintType);
        typed.recycle();

        Random rnd = new Random();
        int color = Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));
        examplePaint = PaintStyles.getStyleFromAttrs(which, color, getContext());
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        ButterKnife.bind(this);
        Random rnd = new Random();

        typefaceTextView.setText(which);
        paintExample.setPaint(examplePaint,
                Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256)));
    }
}
