package milespeele.canvas.util;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by milespeele on 7/5/15.
 */
public class Datastore {

    private SharedPreferences encryptedSharedPreferences;
    private final static String SHARED_PREFS_KEY = "prefs";
    private final static String USERNAME_KEY = "user";
    private final static String PASSWORD_KEY = "pass";

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

    public void setUsername(String username) {
        SharedPreferences.Editor editor = getEditor();
        editor.putString(USERNAME_KEY, username);
        editor.apply();
    }

    public void setPassword(String password) {
        SharedPreferences.Editor editor = getEditor();
        editor.putString(PASSWORD_KEY, password);
        editor.apply();
    }
}
