package milespeele.canvas.util;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Matrix;
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
    public final static float MAX_ALPHA = 255f;
    private final static int DEFAULT_VISBILITY_DURATION = 350;

    private final static Random random = new Random();

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

    public static float relativeCenterX(View view) {
        return (view.getLeft() + view.getRight()) / 2f;
    }

    public static float relativeCenterY(View view) {
        return (view.getTop() + view.getBottom()) / 2f;
    }

    public static Rect boundingRect(float centerX, float centerY, float radius) {
        Rect rect = new Rect();
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

    public static void setIdentityMatrix(Matrix matrix) {
        float[] values = new float[] {1, 0, 0, 0, 1, 0, 0, 0, 1};
        matrix.setValues(values);
    }

    public static void gone(View view, int duration) {
        goneAnimator(view).setDuration(duration).start();
    }

    public static void gone(View view) {
        goneAnimator(view).start();
    }

    public static ObjectAnimator goneAnimator(View view) {
        ObjectAnimator gone = ObjectAnimator.ofFloat(view, View.ALPHA, 1f, 0f);
        gone.setDuration(DEFAULT_VISBILITY_DURATION);
        gone.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                view.setVisibility(View.GONE);
            }
        });
        return gone;
    }

    public static void visible(View view, int duration) {
        if (view.getVisibility() != View.VISIBLE) {
            visibleAnimator(view).setDuration(duration).start();
        }
    }

    public static void visible(View view) {
        if (view.getVisibility() != View.VISIBLE) {
            visibleAnimator(view).start();
        }
    }

    private static ObjectAnimator visibleAnimator(View view) {
        ObjectAnimator visibility = ObjectAnimator.ofFloat(view, View.ALPHA, 0f, 1f);
        visibility.setDuration(DEFAULT_VISBILITY_DURATION);
        visibility.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                view.setVisibility(View.VISIBLE);
            }
        });
        return visibility;
    }
}
