package milespeele.canvas.util;

import android.graphics.RectF;
import android.util.Log;

/**
 * Created by milespeele on 7/3/15.
 */
public class Logg {

    private static final String LOG_TAG = "Miles";

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
    }

    public static void log(Double... doubles) {
        StringBuilder builder = new StringBuilder();
        for (double doubleVal: doubles) {
            builder.append(doubleVal);
            builder.append(", ");
        }
        mainLog(builder.toString());
    }

    public static void log(Double value) {
        mainLog(value.toString());
    }

    public static void log(int[] array) {
        StringBuilder builder = new StringBuilder();
        for (int integer: array) {
            builder.append(String.valueOf(integer));
            builder.append(", ");
        }
        mainLog(builder.toString());
    }

    public static void log(Integer... integers) {
        StringBuilder builder = new StringBuilder();
        for (int integer: integers) {
            builder.append(String.valueOf(integer));
            builder.append(", ");
        }
        mainLog(builder.toString());
    }

    public static void log(Float... integers) {
        StringBuilder builder = new StringBuilder();
        for (float integer: integers) {
            builder.append(String.valueOf(integer));
            builder.append(", ");
        }
        mainLog(builder.toString());
    }

    public static void log(boolean... bools) {
        StringBuilder builder = new StringBuilder();
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
        StringBuilder builder = new StringBuilder();
        for (String toPrint: strings) {
            builder.append(toPrint);
        }
        mainLog(builder.toString());
    }

    public static void log(Throwable throwable, String... strings) {
        StringBuilder builder = new StringBuilder();
        for (String toPrint: strings) {
           builder.append(toPrint);
        }
        mainLog(builder.toString() + throwable.getLocalizedMessage());
    }

    public static void log(Throwable throwable) {
        mainLog(throwable.toString());
    }
}