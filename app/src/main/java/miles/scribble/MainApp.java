package miles.scribble;

import android.app.Application;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import miles.scribble.dagger.ApplicationComponent;
import miles.scribble.dagger.ApplicationModule;
import miles.scribble.dagger.DaggerApplicationComponent;

/**
 * Created by milespeele on 7/3/15.
 */
public class MainApp extends Application {

    private ApplicationComponent component;

    @Override
    public void onCreate() {
        super.onCreate();

        RealmConfiguration realmConfiguration = new RealmConfiguration.Builder()
                .build();

        Realm.setDefaultConfiguration(realmConfiguration);

        component = DaggerApplicationComponent.builder()
                .applicationModule(new ApplicationModule(this))
                .build();
    }

    public ApplicationComponent getApplicationComponent() {
        return component;
    }
}