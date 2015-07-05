package milespeele.canvas;

import android.app.Application;

import com.parse.Parse;
import com.parse.ParseObject;

import milespeele.canvas.dagger.ApplicationComponent;
import milespeele.canvas.dagger.ApplicationModule;
import milespeele.canvas.dagger.DaggerApplicationComponent;
import milespeele.canvas.model.Masterpiece;

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
                getResources().getString(R.string.parse_application_id),
                getResources().getString(R.string.parse_client_key));
        component = DaggerApplicationComponent.builder()
                .applicationModule(new ApplicationModule(this))
                .build();
    }

    public ApplicationComponent getApplicationComponent() {
        return component;
    }

}
