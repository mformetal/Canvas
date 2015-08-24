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
import milespeele.canvas.util.Logg;

/**
 * Created by Miles Peele on 7/26/2015.
 */
public class ViewPaintExample extends View {

    private String which;
    private Paint examplePaint;
    private Path examplePath;
    private Paint borderPaint;

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
        int color = Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));
        examplePaint = PaintStyles.getStyleFromAttrs(which, color, getContext());

        examplePath = new Path();

//        borderPaint = PaintStyles.normalPaint(Color.WHITE, 5f);
    }

    @Override
    public void onFinishInflate() {
        super.onFinishInflate();
        ButterKnife.bind(this);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
//        canvas.drawRect(0, 0, canvas.getWidth(), canvas.getHeight(), borderPaint);
//        canvas.drawLine(canvas.getWidth() / 4, Math.round(canvas.getHeight() * .75),
//                Math.round(canvas.getWidth()  * .75), Math.round(canvas.getWidth() * .25), examplePaint);
        examplePath.moveTo(canvas.getWidth() / 4, Math.round(canvas.getHeight() * .75));
        examplePath.lineTo(Math.round(canvas.getWidth() * .75), Math.round(canvas.getWidth() * .25));
        canvas.drawPath(examplePath, examplePaint);
//        canvas.drawPaint(examplePaint);
    }
}
