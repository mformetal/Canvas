package milespeele.canvas.dagger;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;

import com.squareup.picasso.Cache;
import com.squareup.picasso.LruCache;
import com.squareup.picasso.Picasso;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import de.greenrobot.event.EventBus;
import milespeele.canvas.parse.ParseUtils;
import milespeele.canvas.util.Datastore;

import static android.content.Context.ACTIVITY_SERVICE;
import static android.content.pm.ApplicationInfo.FLAG_LARGE_HEAP;
import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES.HONEYCOMB;

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
        return new Picasso.Builder(mApplication).build();
    }

    @Provides
    @Singleton
    public EventBus getEventBus() {
        return new EventBus();
    }

}
