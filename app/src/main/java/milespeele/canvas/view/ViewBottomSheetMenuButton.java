package milespeele.canvas.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Build;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.Button;

import milespeele.canvas.R;
import milespeele.canvas.paint.PaintStyles;
import milespeele.canvas.util.Util;

/**
 * Created by Miles Peele on 8/2/2015.
 */
public class ViewBottomSheetMenuButton extends Button {

    private Path path;
    private Paint paint;
    private static float offset;

    public ViewBottomSheetMenuButton(Context context) {
        super(context);
        init();
    }

    public ViewBottomSheetMenuButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ViewBottomSheetMenuButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public ViewBottomSheetMenuButton(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        setPadding(0, Math.round(getResources().getDimension(R.dimen.padding_button_top)), 0, 0);
        setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);

        int[] attrs = new int[]{R.attr.selectableItemBackground};
        TypedArray typedArray = getContext().obtainStyledAttributes(attrs);
        int backgroundResource = typedArray.getResourceId(0, 0);
        setBackgroundResource(backgroundResource);
        typedArray.recycle();

        setTextColor(Color.WHITE);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            setTextSize(15f);
            setTypeface(Util.getStaticTypeFace(getContext(), "Roboto.ttf"));
        }

        path = new Path();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        offset = w / 20;
        paint = PaintStyles.normalPaint(Color.WHITE, offset);
        paint.setColorFilter(createDimFilter());
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int width = canvas.getWidth(), height = canvas.getHeight();
        path.moveTo(0 + offset * 2, height - offset);
        path.lineTo(0 + offset, height - offset);
        path.lineTo(0 + offset, height - offset * 2);
        canvas.drawPath(path, paint);

        path.moveTo(0 + offset, 0 + offset * 2);
        path.lineTo(0 + offset, 0 + offset);
        path.lineTo(0 + offset * 2, 0 + offset);
        canvas.drawPath(path, paint);

        path.moveTo(width - offset * 2, 0 + offset);
        path.lineTo(width - offset, 0 + offset);
        path.lineTo(width - offset, 0 + offset * 2);
        canvas.drawPath(path, paint);

        path.moveTo(width - offset, height - offset * 2);
        path.lineTo(width - offset, height - offset);
        path.lineTo(width - offset * 2, height - offset);
        canvas.drawPath(path, paint);
    }

    private ColorFilter createDimFilter() {
        ColorMatrix colorMatrix = new ColorMatrix();
        colorMatrix.setSaturation(0f);
        float scale = 0.5f;
        colorMatrix.setScale(scale, scale, scale, 1f);
        return new ColorMatrixColorFilter(colorMatrix);
    }
}
