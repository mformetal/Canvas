package miles.scribble.data;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;

/**
 * Created by milespeele on 7/5/15.
 */
public class Datastore {

    private SharedPreferences preferences;

    private final static String SHARED_PREFS_KEY = "prefs";
    private final static String BACKGROUND = "background";
    private final static String REVEAL_X = "revealx";
    private final static String REVEAL_Y = "revealy";

    public Datastore(Application application) {
        preferences = application.getSharedPreferences(SHARED_PREFS_KEY, Context.MODE_PRIVATE);
    }

    private SharedPreferences.Editor getEditor() {
        return preferences.edit();
    }

    private SharedPreferences getPrefs() {
        return preferences;
    }

    public void setLastBackgroundColor(int color) {
        getEditor().putInt(BACKGROUND, color).commit();
    }

    public int getLastBackgroundColor() {
        return getPrefs().getInt(BACKGROUND, Color.WHITE);
    }

}
