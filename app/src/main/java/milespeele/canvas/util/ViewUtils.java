package milespeele.canvas.util;

import android.graphics.Color;
import android.util.Property;
import android.view.View;

/**
 * Created by mbpeele on 11/4/15.
 */
public class ViewUtils {

    public static String BACKGROUND = "backgroundColor";
    public static String ALPHA = "alpha";

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

    public static float getCenterX(View view) {
        return (view.getLeft() + view.getRight()) / 2;
    }

    public static float getCenterY(View view) {
        return (view.getTop() + view.getBottom()) / 2;
    }
}
