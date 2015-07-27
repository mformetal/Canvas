package milespeele.canvas.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.widget.TextView;

import butterknife.ButterKnife;
import butterknife.InjectView;
import milespeele.canvas.R;
import milespeele.canvas.paint.PaintStyles;

/**
 * Created by Miles Peele on 7/26/2015.
 */
public class ViewPaintExample extends FrameLayout {

    @InjectView(R.id.paint_example_name) TextView name;

    private String which;
    private Paint paint;

    public ViewPaintExample(Context context) {
        super(context);
        init(context, null);
    }

    public ViewPaintExample(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public ViewPaintExample(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public ViewPaintExample(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        inflate(context, R.layout.paint_example, this);
        if (attrs != null) {
            TypedArray typed = context.obtainStyledAttributes(attrs, R.styleable.ViewPantExample);
            which = typed.getString(R.styleable.ViewPantExample_paintType);
        }
    }

    @Override
    public void onFinishInflate() {
        super.onFinishInflate();
        ButterKnife.inject(this);
        name.setText(which);
        paint = PaintStyles.getStyleFromAttrs(which, getContext());
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawPaint(paint);
    }
}
