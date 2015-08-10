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
import android.view.View;

import java.util.Random;

import butterknife.ButterKnife;
import milespeele.canvas.R;
import milespeele.canvas.paint.PaintStyles;

/**
 * Created by Miles Peele on 7/26/2015.
 */
public class ViewPaintExample extends View {

    private String which;
    private int offset;

    private Path path;
    private Paint examplePaint;
    private Paint demarcationPaint;

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
        TypedArray typed = context.obtainStyledAttributes(attrs, R.styleable.ViewPantExample);
        which = typed.getString(R.styleable.ViewPantExample_paintType);
        typed.recycle();

        Random rnd = new Random();
        examplePaint = PaintStyles.getStyleFromAttrs(which,
                (Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256))),
                getContext());
        examplePaint.setStyle(Paint.Style.FILL_AND_STROKE);

        path = new Path();

        demarcationPaint = PaintStyles.normalPaint(Color.WHITE, 5f);
    }

    @Override
    public void onFinishInflate() {
        super.onFinishInflate();
        ButterKnife.bind(this);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        offset = w / 20;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int width = canvas.getWidth(), height = canvas.getHeight();
        path.moveTo(offset * 2, height - offset);
        path.lineTo(offset, height - offset);
        path.lineTo(offset, height - offset * 2);
        canvas.drawPath(path, demarcationPaint);

        path.moveTo(offset, offset * 2);
        path.lineTo(offset, offset);
        path.lineTo(offset * 2, offset);
        canvas.drawPath(path, demarcationPaint);

        path.moveTo(width - offset * 2, offset);
        path.lineTo(width - offset, offset);
        path.lineTo(width - offset, offset * 2);
        canvas.drawPath(path, demarcationPaint);

        path.moveTo(width - offset, height - offset * 2);
        path.lineTo(width - offset, height - offset);
        path.lineTo(width - offset * 2, height - offset);
        canvas.drawPath(path, demarcationPaint);

        canvas.drawCircle(canvas.getWidth() / 2, canvas.getHeight() / 2, canvas.getWidth() / 4, examplePaint);
    }
}
