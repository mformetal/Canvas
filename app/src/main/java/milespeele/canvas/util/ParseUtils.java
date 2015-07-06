package milespeele.canvas.util;

import android.app.Application;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.parse.SignUpCallback;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.UUID;

import javax.inject.Inject;

import milespeele.canvas.MainApp;
import milespeele.canvas.activity.ActivityHome;
import milespeele.canvas.model.Masterpiece;

/**
 * Created by milespeele on 7/4/15.
 */
public class ParseUtils {

    @Inject Datastore datastore;

    private final static String PINNED_USER = "pinuser";

    public ParseUtils(Application mApplication) {
        ((MainApp) mApplication).getApplicationComponent().inject(this);
    }

    public void checkActiveUser() {
        ParseQuery<ParseUser> userQuery = ParseUser.getQuery();
        userQuery.fromLocalDatastore();
        userQuery.findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> list, ParseException e) {
                if (e == null) {
                    if (list.isEmpty()) {
                        signupNewUser();
                    }
                } else {
                    ParseErrorHandler.handleParseError(e);
                }
            }
        });
    }

    private void signupNewUser() {
        final String username = UUID.randomUUID().toString();
        final String password = UUID.randomUUID().toString();

        final ParseUser user = new ParseUser();
        user.setUsername(username);
        user.setPassword(password);
        user.signUpInBackground(new SignUpCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    datastore.setUsername(username);
                    datastore.setPassword(password);
                    user.pinInBackground(PINNED_USER);
                } else {
                    ParseErrorHandler.handleParseError(e);
                }
            }
        });
    }

    public void saveImage(final WeakReference<ActivityHome> weakCxt, byte[] result) {
        final ParseFile photoFile = new ParseFile(ParseUser.getCurrentUser().getUsername(), result);
        photoFile.saveInBackground(new SaveCallback() {

            @Override
            public void done(ParseException e) {
                if (e == null) {
                    final Masterpiece art = new Masterpiece();
                    art.setImage(photoFile);
                    art.saveEventually(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (e == null) {
                                ParseUser.getCurrentUser().getRelation("Masterpieces").add(art);
                                ParseUser.getCurrentUser().saveEventually(new SaveCallback() {
                                    @Override
                                    public void done(ParseException e) {
                                        if (e == null) {
                                            ActivityHome activityHome = weakCxt.get();
                                            if (activityHome != null) {
                                                Toast.makeText(activityHome,
                                                        "Saved :)",
                                                        Toast.LENGTH_SHORT).show();
                                            }
                                        } else {
                                            ParseErrorHandler.handleParseError(e);
                                        }
                                    }
                                });
                            } else {
                                ParseErrorHandler.handleParseError(e);
                            }
                        }
                    });
                } else {
                    ParseErrorHandler.handleParseError(e);
                }
            }

        });
    }
}
