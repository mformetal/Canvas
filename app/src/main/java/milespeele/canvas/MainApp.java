package milespeele.canvas;

import android.app.Application;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.core.CrashlyticsCore;
import com.parse.Parse;
import com.parse.ParseObject;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterCore;

import io.fabric.sdk.android.Fabric;
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
        Parse.initialize(this,
                getResources().getString(R.string.parse_id),
                getResources().getString(R.string.parse_key));

        Crashlytics crashlyticsKit = new Crashlytics.Builder()
                .core(new CrashlyticsCore.Builder().disabled(BuildConfig.DEBUG).build())
                .build();

        TwitterAuthConfig twitterAuthConfig =
                new TwitterAuthConfig(getResources().getString(R.string.twitter_consumer_key),
                        getResources().getString(R.string.twitter_consumer_secret));

        Fabric.with(this, crashlyticsKit, new TwitterCore(twitterAuthConfig));

        component = DaggerApplicationComponent.builder()
                .applicationModule(new ApplicationModule(this))
                .build();
    }

    public ApplicationComponent getApplicationComponent() {
        return component;
    }

}
