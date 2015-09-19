package milespeele.canvas.paint;

import android.content.Context;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.ComposePathEffect;
import android.graphics.ComposeShader;
import android.graphics.CornerPathEffect;
import android.graphics.DashPathEffect;
import android.graphics.DiscretePathEffect;
import android.graphics.EmbossMaskFilter;
import android.graphics.LightingColorFilter;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Shader;
import android.graphics.SweepGradient;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Random;

import milespeele.canvas.R;
import milespeele.canvas.util.Logg;

/**
 * Created by Miles Peele on 7/26/2015.
 */
public class PaintStyles {

    private static int[] rainbowColors;

    private final static ComposePathEffect composePathEffect = new ComposePathEffect(
            new DashPathEffect(new float[] {1, 51}, 0),
            new CornerPathEffect(1f));

    public static Paint getStyleFromName(String name, int color) {
        for (Method method: PaintStyles.class.getDeclaredMethods()) {
            try {
                if (method.getName().equals(name)) {
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

    public static Paint emboss(int currentColor, float width) {
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setDither(true);
        paint.setColor(currentColor);
        paint.setStrokeWidth(width);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setMaskFilter(new EmbossMaskFilter(new float[]{1, 1, 1}, 0.4f, 6, 3.5f));
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
        paint.setMaskFilter(new EmbossMaskFilter(new float[]{0f, -1f, 0.5f}, 0.8f, 15f, 1f));
        return paint;
    }

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

    public static Paint rainbow(int currentColor, float width) {
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setDither(true);
        paint.setColor(currentColor);
        paint.setStrokeWidth(width);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeCap(Paint.Cap.ROUND);

        if (rainbowColors == null) {
            rainbowColors = new int[] {
                    Color.RED,
                    Color.parseColor("#ffa500"),
                    Color.YELLOW,
                    Color.GREEN,
                    Color.BLUE,
                    Color.parseColor("#4B0082"),
                    Color.parseColor("#8F5E99")
            };
        }

        float[] positions = new float[] {
                (float) Math.random(),
                (float) Math.random(),
                (float) Math.random(),
                (float) Math.random(),
                (float) Math.random(),
                (float) Math.random(),
                (float) Math.random()
        };

        Shader shader = new LinearGradient(0, 0, 0,
                paint.getStrokeWidth() * rainbowColors.length, rainbowColors,
                positions, Shader.TileMode.MIRROR);
        Matrix matrix = new Matrix();
        matrix.setRotate(90);
        shader.setLocalMatrix(matrix);
        paint.setShader(shader);
        return paint;
    }

    public static Paint jagged(int color, float width) {
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setDither(true);
        paint.setColor(color);
        paint.setStrokeWidth(width);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setPathEffect(new DiscretePathEffect(5, 50));
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
