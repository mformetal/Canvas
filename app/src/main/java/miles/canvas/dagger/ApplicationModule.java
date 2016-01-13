package miles.canvas.dagger;

import android.app.Application;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import de.greenrobot.event.EventBus;
import miles.canvas.data.Datastore;

/**
 * Created by milespeele on 7/5/15.
 */
@Module
@Singleton
public class ApplicationModule {

    private Application mApplication;

    public ApplicationModule(Application application) {
        mApplication = application;
    }

    @Provides
    @Singleton
    public Application provideAppContext() {
        return mApplication;
    }

    @Provides
    @Singleton
    public Datastore getDatastore(Application mApplication) {
        return new Datastore(mApplication);
    }

    @Provides
    @Singleton
    public EventBus getEventBus() {
        return new EventBus();
    }
}
