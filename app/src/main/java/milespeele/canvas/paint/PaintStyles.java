package milespeele.canvas.paint;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.BlurMaskFilter;
import android.graphics.DashPathEffect;
import android.graphics.EmbossMaskFilter;
import android.graphics.Paint;
import android.graphics.PathEffect;

import milespeele.canvas.R;

/**
 * Created by Miles Peele on 7/26/2015.
 */
public class PaintStyles {

    private static final BlurMaskFilter SOLID_BLUR_MASK_FILTER
            = new BlurMaskFilter(10, BlurMaskFilter.Blur.SOLID);
    private static final EmbossMaskFilter EMBOSS_MASK_FILTER
            = new EmbossMaskFilter(new float[] {0.0f, -1.0f, 0.5f}, 0.8f, 15f, 1f);
    private static final PathEffect DASHED_PASH_EFFECT
            = new DashPathEffect(new float[] {10,20}, 0);

    public static Paint getStyleFromAttrs(String type, int color, Context context) {
        Resources resources = context.getResources();
        if (type.equals(resources.getString(R.string.paint_example_dashed))) {
            return dashedPaint(color, 15f);
        } else if (type.equals(resources.getString(R.string.paint_example_fade))) {
            return fadePaint(color, 15f);
        } else if (type.equals(resources.getString(R.string.paint_example_neon))) {
            return neonPaint(color, 15f);
        } else {
            return normalPaint(color, 15f);
        }
    }

    public static Paint normalPaint(int currentColor, float width) {
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setDither(true);
        paint.setColor(currentColor);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeWidth(width);
        paint.setStrokeCap(Paint.Cap.ROUND);
        return paint;
    }

    public static Paint fadePaint(int currentColor, float width) {
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setDither(true);
        paint.setColor(currentColor);
        paint.setStrokeWidth(width);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setMaskFilter(SOLID_BLUR_MASK_FILTER);
        return paint;
    }

    public static Paint neonPaint(int currentColor, float width) {
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setDither(true);
        paint.setColor(currentColor);
        paint.setStrokeWidth(width);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setMaskFilter(EMBOSS_MASK_FILTER);
        return paint;
    }

    public static Paint dashedPaint(int currentColor, float width) {
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setDither(true);
        paint.setColor(currentColor);
        paint.setStrokeWidth(width);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setPathEffect(DASHED_PASH_EFFECT);
        return paint;
    }

    public static Paint eraserPaint(int color, float width) {
        Paint eraser = new Paint();
        eraser.setColor(color);
        eraser.setStrokeWidth(width);
        eraser.setStrokeJoin(Paint.Join.ROUND);
        eraser.setStrokeCap(Paint.Cap.ROUND);
        eraser.setStyle(Paint.Style.STROKE);
        return eraser;
    }
}
