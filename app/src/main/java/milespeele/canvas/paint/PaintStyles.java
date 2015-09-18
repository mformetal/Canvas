package milespeele.canvas.paint;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.BlurMaskFilter;
import android.graphics.ComposePathEffect;
import android.graphics.CornerPathEffect;
import android.graphics.DashPathEffect;
import android.graphics.EmbossMaskFilter;
import android.graphics.Paint;
import android.graphics.PathEffect;

import milespeele.canvas.R;

/**
 * Created by Miles Peele on 7/26/2015.
 */
public class PaintStyles {

    private static String[] paintNames;

    private final static ComposePathEffect composePathEffect = new ComposePathEffect(
            new DashPathEffect(new float[] {1, 51}, 0),
            new CornerPathEffect(1f));

    public static Paint getStyleFromAttrs(String type, int color, Context context) {
        if (paintNames == null) {
            paintNames = context.getResources().getStringArray(R.array.paint_examples);
        }
        for (String name: paintNames) {
            if (type.equals(name)) {
                if (type.equals("Dashed")) { return dashedPaint(color, 5f); }
                if (type.equals("Normal")) { return normalPaint(color, 5f); }
            }
        }
        return normalPaint(color, 5f);
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
        paint.setAlpha(0x80);
        paint.setColor(currentColor);
        paint.setStrokeWidth(width);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeCap(Paint.Cap.ROUND);
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
        paint.setMaskFilter(new EmbossMaskFilter(new float[] { 0f, 1f, 0.5f }, 0.8f, 3f, 3f));
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
        paint.setPathEffect(composePathEffect);
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
