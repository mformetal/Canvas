package milespeele.canvas.parse;

import android.app.Application;

import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseRelation;
import com.parse.ParseUser;

import java.lang.ref.WeakReference;
import java.util.List;

import javax.inject.Inject;

import milespeele.canvas.MainApp;
import milespeele.canvas.activity.ActivityHome;
import milespeele.canvas.util.Datastore;
import milespeele.canvas.util.Logg;
import rx.Observable;

/**
 * Created by milespeele on 7/4/15.
 */
public class ParseUtils {

    @Inject Datastore datastore;

    public ParseUtils(Application mApplication) {
        ((MainApp) mApplication).getApplicationComponent().inject(this);
    }

    public void saveImageToServer(String filename, final WeakReference<ActivityHome> weakCxt, byte[] result) {
        final ParseFile photoFile = new ParseFile(ParseUser.getCurrentUser().getUsername(), result);
        photoFile.saveInBackground((ParseException e) -> {
            if (e == null) {
                final Masterpiece art = new Masterpiece();
                art.setImage(photoFile);
                art.setName(filename);
                art.saveEventually(e1 -> {
                    if (e1 == null) {
                        ParseUser.getCurrentUser().getRelation("Masterpieces").add(art);
                        ParseUser.getCurrentUser().saveEventually(e2 -> {
                            if (e2 == null) {
                                ActivityHome activityHome = weakCxt.get();
                                if (activityHome != null && !activityHome.isFinishing()) {
                                    activityHome.showSavedImageSnackbar(art);
                                }
                            } else {
                                handleParseError(e2);
                            }
                        });
                    } else {
                        handleParseError(e1);
                    }
                });
            } else {
                handleParseError(e);
            }
        });
    }

    public Observable<List<Masterpiece>> getSavedMasterpieces() throws ParseException {
        ParseRelation<Masterpiece> relation = ParseUser.getCurrentUser().getRelation("Masterpieces");
        return Observable.just(relation.getQuery().find());
    }

    public void handleParseError(ParseException e) {
        if (e != null) {
            switch (e.getCode()) {
                default:
                    Logg.log("UNHANDLED PARSE EXCEPTION: " + e.getCode());
                    e.printStackTrace();
            }
        }
    }
}
