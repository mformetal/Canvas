package milespeele.canvas.util;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Point;
import android.util.DisplayMetrics;
import android.util.Property;
import android.view.Display;
import android.view.View;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by mbpeele on 11/4/15.
 */
public class ViewUtils {

    public final static String BACKGROUND = "backgroundColor";
    public final static String ALPHA = "alpha";
    public final static String ROTATION = "rotation";
    public final static String TRANSLATION_X = "translationX";
    public final static String TRANSLATION_Y = "translationY";
    public final static String SCALE_Y = "scaleY";
    private static int[] rainbow;

    public static abstract class FloatProperty<T> extends Property<T, Float> {
        public FloatProperty(String name) {
            super(Float.class, name);
        }

        public abstract void setValue(T object, float value);

        @Override
        final public void set(T object, Float value) {
            setValue(object, value);
        }
    }

    public static abstract class IntProperty<T> extends Property<T, Integer> {

        public IntProperty(String name) {
            super(Integer.class, name);
        }

        public abstract void setValue(T object, int value);

        @Override
        final public void set(T object, Integer value) {
            setValue(object, value);
        }

    }

    public static int darken(int color, double fraction) {
        int red = Color.red(color);
        int green = Color.green(color);
        int blue = Color.blue(color);
        return Color.argb(Color.alpha(color),
                (int)Math.max(red - (red * fraction), 0),
                (int)Math.max(green - (green * fraction), 0),
                (int)Math.max(blue - (blue * fraction), 0));
    }

    public static int getComplementaryColor(int color) {
        float[] hsv = new float[3];
        Color.RGBToHSV(Color.red(color), Color.green(color), Color.blue(color),
                hsv);
        if (hsv[2] < 0.5) {
            hsv[2] = 0.7f;
        } else {
            hsv[2] = 0.3f;
        }
        hsv[1] = hsv[1] * 0.2f;
        return Color.HSVToColor(hsv);
    }

    public static int centerX(View view) {
        return (view.getLeft() + view.getRight()) / 2;
    }

    public static int centerY(View view) {
        return (view.getTop() + view.getBottom()) / 2;
    }

    public static int[] rainbow() {
        if (rainbow == null) {
            return (rainbow = new int[]{
                    Color.RED,
                    Color.parseColor("#FF7F00"), // ORANGE
                    Color.YELLOW,
                    Color.parseColor("#7FFF00"), // CHARTREUSE GREEN
                    Color.GREEN,
                    Color.parseColor("#00FF7F"), // SPRING GREEN
                    Color.CYAN,
                    Color.parseColor("#007FFF"), // AZURE
                    Color.BLUE,
                    Color.parseColor("#7F00FF"), // VIOLET
                    Color.MAGENTA,
                    Color.parseColor("#FF007F"), // ROSE
            });
        } else {
            return rainbow;
        }
    }

    public static int getScreenWidth(Context context) {
        Display display = ((Activity) context).getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return size.x;
    }

    public static int getScreenHeight(Context context) {
        Display display = ((Activity) context).getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return size.y;
    }

    public static float dpToPx(float dp, Context context) {
        DisplayMetrics metric = context.getResources().getDisplayMetrics();
        return dp * (metric.densityDpi / 160f);
    }

    public static float pxToDp(float px, Context context) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return px / (metrics.densityDpi / 160f);
    }
}
