package milespeele.canvas.view;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.TextView;

import milespeele.canvas.R;
import milespeele.canvas.paint.PaintStyles;
import milespeele.canvas.util.FontUtils;

/**
 * Created by mbpeele on 9/2/15.
 */
public class ViewTypefaceTextView extends TextView {

    private Paint borderPaint;

    public ViewTypefaceTextView(Context context) {
        super(context);
        init(null);
    }

    public ViewTypefaceTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public ViewTypefaceTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public ViewTypefaceTextView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        setTypeface(FontUtils.getStaticTypeFace(getContext(), "Roboto.ttf"));

        if (attrs != null) {
            TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.ViewTypefaceTextView);
            if (typedArray.getBoolean(R.styleable.ViewTypefaceTextView_shouldDrawBorder, false)) {
                int borderColor = typedArray.getColor(R.styleable.ViewTypefaceTextView_borderColor, Color.GRAY);
                borderPaint = PaintStyles.normal(borderColor, 5f);
            }
            typedArray.recycle();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawBorder(canvas);
    }

    private void drawBorder(Canvas canvas) {
        if (borderPaint != null) {
            canvas.drawLine(0, 0, canvas.getWidth(), 0, borderPaint);
            canvas.drawLine(0, canvas.getHeight(), canvas.getWidth(), canvas.getHeight(), borderPaint);
            canvas.drawLine(0, 0, 0, canvas.getHeight(), borderPaint);
            canvas.drawLine(canvas.getWidth(), 0, canvas.getWidth(), canvas.getHeight(), borderPaint);
        }
    }

    public String getTextAsString() {
        return getText().toString();
    }
}
