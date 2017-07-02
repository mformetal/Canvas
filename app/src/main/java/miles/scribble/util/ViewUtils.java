package miles.scribble.util;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.util.Property;
import android.view.View;

import java.util.Random;

/**
 * Created by mbpeele on 11/4/15.
 */
public class ViewUtils {

    public final static String BACKGROUND = "backgroundColor";
    private final static int DEFAULT_VISBILITY_DURATION = 350;

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
        Random random = new Random();
        return Color.argb(255, random.nextInt(256), random.nextInt(256), random.nextInt(256));
    }

    public static void systemUIGone(View decorView) {
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }

    public static void systemUIVisibile(View decorView) {
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
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

    public static float radius(View view) {
        return view.getMeasuredWidth() / 2f;
    }

    public static void identityMatrix(Matrix matrix) {
        float[] values = new float[] {1, 0, 0, 0, 1, 0, 0, 0, 1};
        matrix.setValues(values);
    }

    public static boolean isVisible(View view) {
        return view.getVisibility() == View.VISIBLE;
    }

    public static void gone(View view, int duration) {
        goneAnimator(view).setDuration(duration).start();
    }

    public static void gone(View view) {
        goneAnimator(view).start();
    }

    public static ObjectAnimator goneAnimator(final View view) {
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

    public static ObjectAnimator visibleAnimator(final View view) {
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
