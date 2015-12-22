package milespeele.canvas.util;

import android.graphics.RectF;
import android.util.Log;

import milespeele.canvas.drawing.DrawingPoint;

/**
 * Created by milespeele on 7/3/15.
 */
public class Logg {

    private static final String LOG_TAG = "Miles";
    private static final StringBuilder builder = new StringBuilder();

    public static void mainLog(String string) {
        if (string != null) {
            if (!string.isEmpty()) {
                Log.d(LOG_TAG, string);
            } else {
                Log.d(LOG_TAG, "printLn needs a Message");
            }
        } else {
            Log.d(LOG_TAG, "Argument to Logg is null");
        }
        builder.setLength(0);
    }

    public static void log(DrawingPoint point) {
        log(point.x, point.y);
    }

    public static void log(DrawingPoint... points) {
        for (DrawingPoint point: points) {
            builder.append("POINT ");
            builder.append(String.valueOf(point.x));
            builder.append(", ");
            builder.append(String.valueOf(point.y));
            builder.append(", ");
        }
        mainLog(builder.toString());
    }

    public static void log(Double... doubles) {
        for (double doubleVal: doubles) {
            builder.append(doubleVal);
            builder.append(", ");
        }
        mainLog(builder.toString());
    }

    public static void log(double[] doubles) {
        for (double doubleVal: doubles) {
            builder.append(doubleVal);
            builder.append(", ");
        }
        mainLog(builder.toString());
    }

    public static void log(float[] floats) {
        for (float val: floats) {
            builder.append(val);
            builder.append(", ");
        }
        mainLog(builder.toString());
    }

    public static void log(Circle circle) {
        builder.append(circle.getCenterX());
        builder.append(", ");
        builder.append(circle.getCenterY());
        builder.append(", ");
        builder.append(circle.getRadius());
        mainLog(builder.toString());
    }

    public static void log(Double value) {
        mainLog(value.toString());
    }

    public static void log(int[] array) {
        for (int integer: array) {
            builder.append(String.valueOf(integer));
            builder.append(", ");
        }
        mainLog(builder.toString());
    }

    public static void log(Integer... integers) {
        for (int integer: integers) {
            builder.append(String.valueOf(integer));
            builder.append(", ");
        }
        mainLog(builder.toString());
    }

    public static void log(Float... integers) {
        for (float integer: integers) {
            builder.append(String.valueOf(integer));
            builder.append(", ");
        }
        mainLog(builder.toString());
    }

    public static void log(boolean... bools) {
        for (boolean bool: bools) {
            builder.append(String.valueOf(bool));
            builder.append(", ");
        }
        mainLog(builder.toString());
    }

    public static void log(boolean bool) {
        mainLog(String.valueOf(bool));
    }

    public static void log(float integer) {
       mainLog(String.valueOf(integer));
    }

    public static void log(int integer) {
        mainLog(String.valueOf(integer));
    }

    public static void log(String string) {
        mainLog(string);
    }

    public static void log(String string, Throwable throwable) {
        mainLog(string + throwable.getLocalizedMessage());
    }

    public static void log(String... strings) {
        for (String toPrint: strings) {
            builder.append(toPrint);
        }
        mainLog(builder.toString());
    }

    public static void log(Throwable throwable, String... strings) {
        for (String toPrint: strings) {
           builder.append(toPrint);
        }
        mainLog(builder.toString() + throwable.getLocalizedMessage());
    }

    public static void log(Throwable throwable) {
        mainLog(throwable.toString());
    }

    public static void log(String string, Circle circle) {
        builder.append(string);
        builder.append(", ");
        log(circle);
    }

    public static void log(String string, float... floats) {
        builder.append(string);
        builder.append(", ");
        log(floats);
    }

    public static void log(String string, double... doubles) {
        builder.append(string);
        builder.append(", ");
        log(doubles);
    }
}