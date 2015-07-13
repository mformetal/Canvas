package milespeele.canvas.parse;

import android.app.Application;

import com.parse.CountCallback;
import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.UUID;

import javax.inject.Inject;

import milespeele.canvas.MainApp;
import milespeele.canvas.activity.ActivityHome;
import milespeele.canvas.util.Datastore;
import milespeele.canvas.util.Logger;
import rx.Observable;

/**
 * Created by milespeele on 7/4/15.
 */
public class ParseUtils {

    @Inject Datastore datastore;

    private final static String PINNED_USER = "pinuser";
    private final static String PINNED_MASTERPIECE = "yeah";

    public ParseUtils(Application mApplication) {
        ((MainApp) mApplication).getApplicationComponent().inject(this);
    }

    public void checkActiveUser() {
        ParseQuery<ParseUser> userQuery = ParseUser.getQuery();
        userQuery.fromLocalDatastore();
        userQuery.findInBackground((list, e) -> {
            if (e == null) {
                if (list.isEmpty()) {
                    signupNewUser();
                }
            } else {
                handleParseError(e);
            }
        });
    }

    private void signupNewUser() {
        final String username = UUID.randomUUID().toString();
        final String password = UUID.randomUUID().toString();

        final ParseUser user = new ParseUser();
        user.setUsername(username);
        user.setPassword(password);
        user.signUpInBackground(e -> {
            if (e == null) {
                datastore.setUsername(username);
                datastore.setPassword(password);
                user.pinInBackground(PINNED_USER);
            } else {
                handleParseError(e);
            }
        });
    }

    private void loginUser() {
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

    public void saveImageToLocalDatastore(byte[] result) {
        ParseObject.unpinAllInBackground(PINNED_MASTERPIECE, e -> {
            if (e == null) {
                final ParseFile photoFile = new ParseFile(ParseUser.getCurrentUser().getUsername(), result);
                photoFile.saveInBackground((ParseException e1) -> {
                    if (e1 == null) {
                        final Masterpiece art = new Masterpiece();
                        art.setImage(photoFile);
                        art.pinInBackground(e2 -> {
                            if (e2 != null) {
                                handleParseError(e1);
                            }
                        });
                    } else {
                        handleParseError(e);
                    }
                });
            } else {
                handleParseError(e);
            }
        });
    }

    public Observable<List<Masterpiece>> getSavedMasterpieces() {
        ParseQuery<Masterpiece> query = ParseQuery.getQuery(Masterpiece.class);
        query.setLimit(30);
        try {
            return Observable.just(query.find());
        } catch (ParseException e) {
            handleParseError(e);
            return null;
        }
    }


    public void handleParseError(ParseException e) {
        switch (e.getCode()) {
            default:
                Logger.log("UNHANDLED PARSE EXCEPTION: " + e.getCode());
                e.printStackTrace();
        }
    }
}
