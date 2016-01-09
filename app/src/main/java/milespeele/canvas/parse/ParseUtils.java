package milespeele.canvas.parse;

import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap;

import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseUser;

import java.lang.ref.WeakReference;

import javax.inject.Inject;

import de.greenrobot.event.EventBus;
import milespeele.canvas.MainApp;
import milespeele.canvas.activity.ActivityHome;
import milespeele.canvas.event.EventParseError;
import milespeele.canvas.util.FileUtils;
import milespeele.canvas.util.Logg;
import rx.Observable;
import rx.Single;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.parse.ParseObservable;
import rx.schedulers.Schedulers;

/**
 * Created by milespeele on 7/4/15.
 */
public class ParseUtils {

    @Inject EventBus bus;

    public final static String MASTERPIECE_RELATION = "Masterpieces";

    public ParseUtils(Application mApplication) {
        ((MainApp) mApplication).getApplicationComponent().inject(this);
    }

    public Observable saveImageToServer(final Context context, final String filename,
                                        final Bitmap bitmap) {
        ParseUser user = ParseUser.getCurrentUser();
        if (user != null && user.isAuthenticated()) {
            return FileUtils.cacheAsObservable(bitmap, context)
                    .flatMap(bytes -> {
                        final ParseFile photoFile = new ParseFile(
                                ParseUser.getCurrentUser().getUsername(), bytes);
                        return ParseObservable.save(photoFile);
                    })
                    .flatMap(parseFile -> {
                        final Masterpiece art = new Masterpiece();
                        art.setImage(parseFile);
                        art.setTitle(filename);
                        return ParseObservable.saveEventually(art);
                    })
                    .flatMap(new Func1<Object, Observable<?>>() {
                        @Override
                        public Observable<?> call(Object o) {
                            ParseUser.getCurrentUser().getRelation(MASTERPIECE_RELATION).add((Masterpiece) o);
                            return ParseObservable.saveEventually(ParseUser.getCurrentUser());
                        }
                    });
        } else {
            return Observable.empty();
        }
    }

    public Observable<ParseUser> login(String username, String password) {
        return ParseObservable.logIn(username, password)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public void handleError(Throwable throwable) {
        throwable.printStackTrace();
        if (throwable instanceof ParseException) {
            ParseException e = (ParseException) throwable;
            Logg.log("PARSE ERROR CODE: " + e.getCode());
            Logg.log("PARSE ERROR MESSAGE: " + e.getMessage());
            bus.post(new EventParseError(e));
        } else {
            bus.post(new EventParseError(throwable));
        }
    }
}
