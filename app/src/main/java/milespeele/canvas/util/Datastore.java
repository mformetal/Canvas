package milespeele.canvas.util;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;

/**
 * Created by milespeele on 7/5/15.
 */
public class Datastore {

    private SharedPreferences encryptedSharedPreferences;

    private final static String SHARED_PREFS_KEY = "prefs";
    private final static String BACKGROUND = "background";

    public Datastore(Application application) {
        encryptedSharedPreferences = application.getSharedPreferences(SHARED_PREFS_KEY,
                Context.MODE_PRIVATE);
    }

    private SharedPreferences.Editor getEditor() {
        return encryptedSharedPreferences.edit();
    }

    private SharedPreferences getPrefs() {
        return encryptedSharedPreferences;
    }

    public void setLastBackgroundColor(int color) {
        getEditor().putInt(BACKGROUND, color).commit();
    }

    public int getLastBackgroundColor() {
        return getPrefs().getInt(BACKGROUND,
                Color.parseColor("#FFFFFF"));
    }

}
