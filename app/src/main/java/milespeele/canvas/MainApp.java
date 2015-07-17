package milespeele.canvas;

import android.app.Application;

import com.parse.Parse;
import com.parse.ParseCrashReporting;
import com.parse.ParseFacebookUtils;
import com.parse.ParseObject;
import com.parse.ParseTwitterUtils;

import milespeele.canvas.dagger.ApplicationComponent;
import milespeele.canvas.dagger.ApplicationModule;
import milespeele.canvas.dagger.DaggerApplicationComponent;
import milespeele.canvas.parse.Masterpiece;

/**
 * Created by milespeele on 7/3/15.
 */
public class MainApp extends Application {

    private ApplicationComponent component;

    @Override
    public void onCreate() {
        super.onCreate();
        ParseObject.registerSubclass(Masterpiece.class);
        Parse.enableLocalDatastore(this);
        ParseCrashReporting.enable(this);
        Parse.initialize(this,
                getResources().getString(R.string.parse_application_id),
                getResources().getString(R.string.parse_client_key));
        ParseFacebookUtils.initialize(this);
        ParseTwitterUtils.initialize(getResources().getString(R.string.twitter_consumer_key),
                getResources().getString(R.string.twitter_consumer_secret));
        component = DaggerApplicationComponent.builder()
                .applicationModule(new ApplicationModule(this))
                .build();
    }

    public ApplicationComponent getApplicationComponent() {
        return component;
    }

}
