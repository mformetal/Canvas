package milespeele.canvas.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
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
    private Paint textPaint;

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

        borderPaint = PaintStyles.normalPaint(Color.GRAY, 5f);

        textPaint = PaintStyles.normalPaint(Color.WHITE, 5f);
        textPaint.setTextAlign(Paint.Align.CENTER);
    }

    @Override
    public void onFinishInflate() {
        super.onFinishInflate();
        ButterKnife.bind(this);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawBorder(canvas);
        drawExamplePaint(canvas);
//        drawText(canvas);
    }

    private void drawExamplePaint(Canvas canvas) {
        examplePath.moveTo(canvas.getWidth() / 4, Math.round(canvas.getHeight() * .75));
        examplePath.lineTo(Math.round(canvas.getWidth() * .75), Math.round(canvas.getWidth() * .25));
        canvas.drawPath(examplePath, examplePaint);
    }

    private void drawBorder(Canvas canvas) {
        canvas.drawLine(0, 0, canvas.getWidth(), 0, borderPaint);
        canvas.drawLine(0, canvas.getHeight(), canvas.getWidth(), canvas.getHeight(), borderPaint);
        canvas.drawLine(0, 0, 0, canvas.getHeight(), borderPaint);
        canvas.drawLine(canvas.getWidth(), 0, canvas.getWidth(), canvas.getHeight(), borderPaint);
    }

    private void drawText(Canvas canvas) {
        setTextSizeForWidth(textPaint, canvas.getWidth(), which);
        canvas.drawText(which, canvas.getWidth() / 2, canvas.getHeight() -
                textPaint.getFontSpacing(), textPaint);
    }

    private void setTextSizeForWidth(Paint paint, float desiredWidth, String text) {
        // Pick a reasonably large value for the test. Larger values produce
        // more accurate results, but may cause problems with hardware
        // acceleration. But there are workarounds for that, too; refer to
        // http://stackoverflow.com/questions/6253528/font-size-too-large-to-fit-in-cache
        final float testTextSize = 48f;

        // Get the bounds of the text, using our testTextSize.
        paint.setTextSize(testTextSize);
        Rect bounds = new Rect();
        paint.getTextBounds(text, 0, text.length(), bounds);

        // Calculate the desired size as a proportion of our testTextSize.
        float desiredTextSize = testTextSize * desiredWidth / bounds.width();

        // Set the paint for that size.
        paint.setTextSize(desiredTextSize);
    }
}
