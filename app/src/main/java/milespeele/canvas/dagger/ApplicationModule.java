package milespeele.canvas.dagger;

import android.app.Application;

import com.squareup.picasso.Picasso;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import milespeele.canvas.parse.ParseUtils;
import milespeele.canvas.util.Datastore;

/**
 * Created by milespeele on 7/5/15.
 */
@Module
@Singleton
public class ApplicationModule {

    private final String SHARED_PREFS_KEY = "prefs";

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
    public ParseUtils getParseUtils(Application mApplication) {
        return new ParseUtils(mApplication);
    }

    @Provides
    @Singleton
    public Picasso getPicasso(Application mApplication) {
        return Picasso.with(mApplication);
    }

}
