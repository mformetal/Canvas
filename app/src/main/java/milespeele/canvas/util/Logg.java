package milespeele.canvas.util;

import android.util.Log;

/**
 * Created by milespeele on 7/3/15.
 */
public class Logg {

    private static final String LOG_TAG = "Miles";

    public static void log(Double value) {
        Log.d(LOG_TAG, value.toString());
    }

    public static void log(int integer, float floater) {
        Log.d(LOG_TAG, String.valueOf(integer) + ", " + String.valueOf(floater));
    }

    public static void log(int[] array) {
        StringBuilder builder = new StringBuilder();
        for (int integer: array) {
            builder.append(String.valueOf(integer));
            builder.append(", ");
        }
        Log.d(LOG_TAG, builder.toString());
    }

    public static void log(Integer... integers) {
        StringBuilder builder = new StringBuilder();
        for (int integer: integers) {
            builder.append(String.valueOf(integer));
            builder.append(", ");
        }
        Log.d(LOG_TAG, builder.toString());
    }

    public static void log(Float... integers) {
        StringBuilder builder = new StringBuilder();
        for (float integer: integers) {
            builder.append(String.valueOf(integer));
            builder.append(", ");
        }
        Log.d(LOG_TAG, builder.toString());
    }

    public static void log(boolean... bools) {
        StringBuilder builder = new StringBuilder();
        for (boolean bool: bools) {
            builder.append(String.valueOf(bool));
            builder.append(", ");
        }
        Log.d(LOG_TAG, builder.toString());
    }

    public static void log(boolean bool) {
        Log.d(LOG_TAG, String.valueOf(bool));
    }

    public static void log(float integer) {
        Log.d(LOG_TAG, String.valueOf(integer));
    }

    public static void log(int integer) {
        Log.d(LOG_TAG, String.valueOf(integer));
    }

    public static void log(int integer, String string) {
        Log.d(LOG_TAG, string + " " + integer);
    }

    public static void log(String string) {
        Log.d(LOG_TAG, string);
    }

    public static void log(String string, Throwable throwable) {
        Log.d(LOG_TAG, string, throwable);
    }

    public static void log(String... strings) {
        for (String toPrint: strings) {
            Log.d(LOG_TAG, toPrint);
        }
    }

    public static void log(Throwable throwable, String... strings) {
        for (String toPrint: strings) {
            Log.d(LOG_TAG, toPrint);
        }

        Log.d(LOG_TAG, "", throwable);
    }

    public static void log(Throwable throwable) {
        throwable.printStackTrace();
        Log.d(LOG_TAG, throwable.getLocalizedMessage());
    }
}