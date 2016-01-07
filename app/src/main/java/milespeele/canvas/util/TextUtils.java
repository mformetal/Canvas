package milespeele.canvas.util;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;

import java.io.IOException;
import java.util.HashMap;

public final class TextUtils {

    private static HashMap<String, Typeface> mFontMap;

    private static void initializeFontMap(Context context) {
        mFontMap = new HashMap<>();
        AssetManager assetManager = context.getAssets();
        try {
            String[] fontFileNames = assetManager.list("fonts");
            for (String fontFileName : fontFileNames) {
                Typeface typeface = Typeface.createFromAsset(assetManager, "fonts/" + fontFileName);
                mFontMap.put(fontFileName, typeface);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static Typeface getStaticTypeFace(Context context, String fontFileName) {
        if (mFontMap == null) {
            initializeFontMap(context);
        }
        Typeface typeface = mFontMap.get(fontFileName);
        if (typeface == null) {
            throw new IllegalArgumentException(
                    "Font name must match file name in assets/fonts/ directory: " + fontFileName);
        }
        return typeface;
    }

    public static void adjustTextSize(Paint textPaint, String text, int height) {
        textPaint.setTextSize(100);
        textPaint.setTextScaleX(1.0f);
        Rect bounds = new Rect();
        textPaint.getTextBounds(text, 0, text.length(), bounds);
        int h = bounds.height();
        float target = (float) height * .2f;
        float size = ((target/h) * 100f);
        textPaint.setTextSize(size);
    }

    public static  void adjustTextScale(Paint textPaint, String text, float width, int paddingLeft,
                                         int paddingRight) {
        textPaint.setTextScaleX(1.0f);

        Rect bounds = new Rect();
        textPaint.getTextBounds(text, 0, text.length(), bounds);
        int w = bounds.width();
        float xscale = (width - paddingLeft - paddingRight) / w;
        textPaint.setTextScaleX(xscale * .6f);
    }

    public static boolean containsCapital(String string) {
        for (int i = 0; i < string.length(); i++) {
            if (Character.isUpperCase(string.charAt(i))) {
                return true;
            }
        }
        return false;
    }

    public static String capitalizeFirst(String string) {
        return string.substring(0, 1).toUpperCase() + string.substring(1, string.length());
    }

    public static boolean containsNewLine(String string) {
        return string.contains("\n");
    }

}