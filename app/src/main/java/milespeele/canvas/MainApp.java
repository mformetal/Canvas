package milespeele.canvas;

import android.app.Application;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.core.CrashlyticsCore;
import com.digits.sdk.android.Digits;
import com.parse.Parse;
import com.parse.ParseObject;

import com.twitter.sdk.android.Twitter;
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

    // Note: Your consumer key and secret should be obfuscated in your source code before shipping.
    private static final String TWITTER_KEY = "lR0Ju85QIIhF6fEApsKtu22oN";
    private static final String TWITTER_SECRET = "SkOFwuoqWSIgs7ZYuhVIKvNbVSt7LwjaBXPCV8GYa35ZFs1LCo";


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

        TwitterAuthConfig authConfig =  new TwitterAuthConfig(getString(R.string.twitter_consumer_key),
                getString(R.string.twitter_consumer_secret));


        Fabric.with(this, crashlyticsKit, new TwitterCore(authConfig));

        component = DaggerApplicationComponent.builder()
                .applicationModule(new ApplicationModule(this))
                .build();
    }

    public ApplicationComponent getApplicationComponent() {
        return component;
    }

}
