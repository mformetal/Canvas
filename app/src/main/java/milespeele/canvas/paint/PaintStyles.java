package milespeele.canvas.paint;

import android.graphics.BlurMaskFilter;
import android.graphics.Color;
import android.graphics.ComposePathEffect;
import android.graphics.CornerPathEffect;
import android.graphics.DashPathEffect;
import android.graphics.DiscretePathEffect;
import android.graphics.EmbossMaskFilter;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Shader;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import milespeele.canvas.util.ViewUtils;

/**
 * Created by Miles Peele on 7/26/2015.
 */
public class PaintStyles {

    private final static ComposePathEffect composePathEffect = new ComposePathEffect(
            new DashPathEffect(new float[] {1, 51}, 0),
            new CornerPathEffect(1f));

    public static Paint getStyleFromName(String name, int color) {
        for (Method method: PaintStyles.class.getDeclaredMethods()) {
            try {
                if (method.getName().equals(name)) {
                    return (Paint) method.invoke(null, color, 5f);
                }
            } catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
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

    public static Paint emboss(int currentColor, float width) {
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setDither(true);
        paint.setColor(currentColor);
        paint.setStrokeWidth(width);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setMaskFilter(new EmbossMaskFilter(new float[]{1, 1, 1}, 0.4f, 10, 8.2f));
        return paint;
    }

    public static Paint deboss(int currentColor, float width) {
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setDither(true);
        paint.setColor(currentColor);
        paint.setStrokeWidth(width);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setMaskFilter(new EmbossMaskFilter(new float[]{0f, -1f, 0.5f}, 0.8f, 13f, 7f));
        return paint;
    }

    public static Paint dots(int currentColor, float width) {
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

    public static Paint rainbow(int currentColor, float width) {
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setDither(true);
        paint.setStrokeWidth(width);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeCap(Paint.Cap.ROUND);
        LinearGradient linearGradient = new LinearGradient(0, 0, 0, 50, ViewUtils.rainbow(), null, Shader.TileMode.MIRROR);
        paint.setShader(linearGradient);
        return paint;
    }

    public static Paint normalShadow(int color, float width) {
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setDither(true);
        paint.setColor(color);
        paint.setStrokeWidth(width);
        paint.setStyle(Paint.Style.STROKE);
        paint.setMaskFilter(new BlurMaskFilter(15, BlurMaskFilter.Blur.NORMAL));
        return paint;
    }

    public static Paint innerShadow(int color, float width) {
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setDither(true);
        paint.setColor(color);
        paint.setStrokeWidth(width);
        paint.setStyle(Paint.Style.STROKE);
        paint.setMaskFilter(new BlurMaskFilter(15, BlurMaskFilter.Blur.INNER));
        return paint;
    }

    public static Paint outerShadow(int color, float width) {
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setDither(true);
        paint.setColor(color);
        paint.setStrokeWidth(width);
        paint.setStyle(Paint.Style.STROKE);
        paint.setMaskFilter(new BlurMaskFilter(15, BlurMaskFilter.Blur.OUTER));
        return paint;
    }

    public static Paint solidShadow(int color, float width) {
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setDither(true);
        paint.setColor(color);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(width);
        paint.setMaskFilter(new BlurMaskFilter(15, BlurMaskFilter.Blur.SOLID));
        return paint;
    }
}