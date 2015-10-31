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
    private final static String REVEAL_X = "revealx";
    private final static String REVEAL_Y = "revealy";

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
        return getPrefs().getInt(BACKGROUND, Color.WHITE);
    }

    public void putTouchRevealCoordinates(float x, float y) {
        getEditor().putFloat(REVEAL_X, x).putFloat(REVEAL_Y, y).commit();
    }

    public float getRevealX() {
        return getPrefs().getFloat(REVEAL_X, 0);
    }

    public float getRevealY() {
        return getPrefs().getFloat(REVEAL_Y, 0);
    }

}
