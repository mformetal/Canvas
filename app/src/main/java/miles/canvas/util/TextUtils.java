package miles.canvas.util;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Typeface;

import java.io.IOException;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    public static boolean validateEmail(String emailStr) {
        Matcher matcher = Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$",
                Pattern.CASE_INSENSITIVE).matcher(emailStr);
        return matcher.find();
    }

}