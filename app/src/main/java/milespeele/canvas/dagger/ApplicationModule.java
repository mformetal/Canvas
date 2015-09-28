package milespeele.canvas.dagger;

import android.app.Application;

import com.squareup.picasso.LruCache;
import com.squareup.picasso.Picasso;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import de.greenrobot.event.EventBus;
import milespeele.canvas.parse.ParseUtils;
import milespeele.canvas.util.Datastore;

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
    public ParseUtils getParseUtils(Application mApplication) {
        return new ParseUtils(mApplication);
    }

    @Provides
    @Singleton
    public Picasso getPicasso(Application mApplication) {
        return new Picasso.Builder(mApplication)
                .memoryCache( new LruCache((int) (Runtime.getRuntime().maxMemory() / 1024) / 8))
                .build();
    }

    @Provides
    @Singleton
    public EventBus getEventBus() {
        return new EventBus();
    }

}
