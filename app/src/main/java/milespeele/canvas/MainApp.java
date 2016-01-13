package milespeele.canvas;

import android.app.Application;

import com.crashlytics.android.Crashlytics;

import java.util.UUID;

import io.fabric.sdk.android.Fabric;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import milespeele.canvas.dagger.ApplicationComponent;
import milespeele.canvas.dagger.ApplicationModule;
import milespeele.canvas.dagger.DaggerApplicationComponent;

/**
 * Created by milespeele on 7/3/15.
 */
public class MainApp extends Application {

    private ApplicationComponent component;

    @Override
    public void onCreate() {
        super.onCreate();
        Fabric.with(this, new Crashlytics());

        RealmConfiguration realmConfiguration = new RealmConfiguration.Builder(this)
                .name("examples.realm")
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
