package milespeele.canvas.paint;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.BlurMaskFilter;
import android.graphics.DashPathEffect;
import android.graphics.EmbossMaskFilter;
import android.graphics.Paint;
import android.graphics.PathEffect;

import java.util.Random;

import milespeele.canvas.R;

/**
 * Created by Miles Peele on 7/26/2015.
 */
public class PaintStyles {

    private static final BlurMaskFilter BLUR_MASK_FILTER = new BlurMaskFilter(5, BlurMaskFilter.Blur.OUTER);
    private static final EmbossMaskFilter EMBOSS_MASK_FILTER = new EmbossMaskFilter(new float[]{1, 1, 1}, 1f, 0, 3);
    private static final PathEffect DASHED_PASH_EFFECT = new DashPathEffect(new float[] {10,20}, 0);

    public static Paint getStyleFromAttrs(String type, int color, Context context) {
        Resources resources = context.getResources();
        if (type == resources.getString(R.string.paint_example_dashed)) {
            return dashedPaint(color, 10f);
        } else if (type ==resources.getString(R.string.paint_example_fade)) {
            return fadePaint(color, 10f);
        } else if (type == resources.getString(R.string.paint_example_neon)) {
            return neonPaint(color, 10f);
        } else {
            return normalPaint(color, 10f);
        }
    }

    public static Paint normalPaint(int currentColor, float width) {
        Paint paint = new Paint();
        paint.setAntiAlias(true);
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
        paint.setMaskFilter(BLUR_MASK_FILTER);
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
