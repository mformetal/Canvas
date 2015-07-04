package milespeele.canvas;

import android.app.Application;

import com.parse.Parse;

/**
 * Created by milespeele on 7/3/15.
 */
public class MainApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Parse.enableLocalDatastore(this);
        Parse.initialize(this,
                getResources().getString(R.string.parse_application_id),
                getResources().getString(R.string.parse_client_key));
    }

}
