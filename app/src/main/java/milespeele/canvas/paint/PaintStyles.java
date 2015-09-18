package milespeele.canvas.paint;

import android.content.Context;
import android.graphics.ComposePathEffect;
import android.graphics.CornerPathEffect;
import android.graphics.DashPathEffect;
import android.graphics.EmbossMaskFilter;
import android.graphics.Paint;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;

import milespeele.canvas.R;
import milespeele.canvas.util.Logg;

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

        for (Method method: PaintStyles.class.getDeclaredMethods()) {
            try {
                if (method.getName().equals(type)) {
                    return (Paint) method.invoke(null, color, 5f);
                }
            } catch (IllegalArgumentException e) {
            } catch (IllegalAccessException e) {
            } catch (InvocationTargetException e) {
            }
        }
        return normal(color, 5f);
    }

    public static Paint normal(int currentColor, float width) {
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

//    public static Paint fadePaint(int currentColor, float width) {
//        Paint paint = new Paint();
//        paint.setAntiAlias(true);
//        paint.setDither(true);
//        paint.setAlpha(0x80);
//        paint.setColor(currentColor);
//        paint.setStrokeWidth(width);
//        paint.setStyle(Paint.Style.STROKE);
//        paint.setStrokeJoin(Paint.Join.ROUND);
//        paint.setStrokeCap(Paint.Cap.ROUND);
//        return paint;
//    }
//
//    public static Paint neonPaint(int currentColor, float width) {
//        Paint paint = new Paint();
//        paint.setAntiAlias(true);
//        paint.setDither(true);
//        paint.setColor(currentColor);
//        paint.setStrokeWidth(width);
//        paint.setStyle(Paint.Style.STROKE);
//        paint.setStrokeJoin(Paint.Join.ROUND);
//        paint.setStrokeCap(Paint.Cap.ROUND);
//        paint.setMaskFilter(new EmbossMaskFilter(new float[] { 0f, 1f, 0.5f }, 0.8f, 3f, 3f));
//        return paint;
//    }

    public static Paint dashed(int currentColor, float width) {
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

    public static Paint erase(int color, float width) {
        Paint eraser = new Paint();
        eraser.setColor(color);
        eraser.setStrokeWidth(width);
        eraser.setStrokeJoin(Paint.Join.ROUND);
        eraser.setStrokeCap(Paint.Cap.ROUND);
        eraser.setStyle(Paint.Style.STROKE);
        return eraser;
    }
}
