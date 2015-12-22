package milespeele.canvas.util;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.DisplayMetrics;
import android.util.Property;
import android.view.Display;
import android.view.View;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import milespeele.canvas.R;

/**
 * Created by mbpeele on 11/4/15.
 */
public class ViewUtils {

    public final static String BACKGROUND = "backgroundColor";
    public final static String ALPHA = "alpha";

    private final static Random random = new Random();
    private final static Rect rect = new Rect();
    private static int[] rainbow;
    private static int[] fullRainbow;

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

    public static int randomColor() {
        return Color.argb(255, random.nextInt(256), random.nextInt(256), random.nextInt(256));
    }

    public static String colorToHexString(int color) {
        return String.format("#%06X", (0xFFFFFF & color));
    }

    public static int getComplimentColor(int color) {
        // get existing colors
        int alpha = Color.alpha(color);
        int red = Color.red(color);
        int blue = Color.blue(color);
        int green = Color.green(color);

        // find compliments
        red = (~red) & 0xff;
        blue = (~blue) & 0xff;
        green = (~green) & 0xff;

        return Color.argb(alpha, red, green, blue);
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

    public static float centerX(View view) {
        return (view.getLeft() + view.getRight()) / 2f;
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

    public static int[] fullRainbow() {
        if (fullRainbow == null) {
            return (fullRainbow = new int[] {
                    Color.WHITE,
                    Color.GRAY,
                    Color.BLACK,
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
            return fullRainbow;
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

    public static float activityVerticalMargin(Context context) {
        return context.getResources().getDimension(R.dimen.activity_vertical_margin);
    }

    public static float dpToPx(float dp, Context context) {
        DisplayMetrics metric = context.getResources().getDisplayMetrics();
        return dp * (metric.densityDpi / 160f);
    }

    public static float pxToDp(float px, Context context) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return px / (metrics.densityDpi / 160f);
    }

    public static Rect boundingRect(float centerX, float centerY, float radius) {
        rect.set(0, 0, 0, 0);
        rect.left = Math.round(centerX - radius);
        rect.right = Math.round(centerX + radius);
        rect.top = Math.round(centerY - radius);
        rect.bottom = Math.round(centerY + radius);
        return rect;
    }

    // ASSUMES VIEW IS A CIRCLE SO WIDTH = HEIGHT
    public static float radius(View view) {
        return view.getMeasuredWidth() / 2f;
    }
}
