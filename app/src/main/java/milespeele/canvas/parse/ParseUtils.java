package milespeele.canvas.parse;

import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap;
import android.support.design.widget.Snackbar;
import android.view.View;

import com.facebook.AccessToken;
import com.parse.FindCallback;
import com.parse.LogInCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseFile;
import com.parse.ParseRelation;
import com.parse.ParseTwitterUtils;
import com.parse.ParseUser;
import com.parse.RequestPasswordResetCallback;
import com.parse.SaveCallback;
import com.parse.SignUpCallback;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;

import javax.inject.Inject;

import bolts.Task;
import de.greenrobot.event.EventBus;
import milespeele.canvas.MainApp;
import milespeele.canvas.R;
import milespeele.canvas.activity.ActivityBase;
import milespeele.canvas.util.FileUtils;
import milespeele.canvas.util.Logg;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.bolts.TaskObservable;
import rx.functions.Func0;
import rx.functions.Func1;
import rx.parse.ParseObservable;
import rx.schedulers.Schedulers;

/**
 * Created by milespeele on 7/4/15.
 */
public class ParseUtils {

    @Inject EventBus bus;

    private final static String MASTERPIECE_RELATION = "Masterpieces";
    private final static String USER_NAME_FIELD = "name";

    public ParseUtils(Application mApplication) {
        ((MainApp) mApplication).getApplicationComponent().inject(this);
    }

    public static String getParseName() {
        ParseUser user = ParseUser.getCurrentUser();
        if (user != null && user.isAuthenticated()) {
            return (String) user.get(USER_NAME_FIELD);
        } else {
            return "Sign In!";
        }
    }

    public static boolean isParseUserAvailable() {
        ParseUser user = ParseUser.getCurrentUser();
        return user != null && user.isAuthenticated();
    }

    public static boolean isLinkedWithFacebook() {
        ParseUser user = ParseUser.getCurrentUser();
        return isParseUserAvailable() && ParseFacebookUtils.isLinked(user);
    }

    public static boolean isLinkedWithTwitter() {
        ParseUser user = ParseUser.getCurrentUser();
        return isParseUserAvailable() && ParseTwitterUtils.isLinked(user);
    }

    public static boolean isLoggedInWithFacebook() {
        AccessToken token = AccessToken.getCurrentAccessToken();
        return token != null && !token.isExpired();
    }

    public Observable uploadMasterpiece(final String filename, final Bitmap bitmap) {
        return FileUtils.compress(bitmap)
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
    }

    public Observable<Masterpiece> getMasterpieces() {
        ParseRelation<Masterpiece> relation = ParseUser.getCurrentUser().getRelation(MASTERPIECE_RELATION);
        return ParseObservable.all(relation.getQuery())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Observable resetParsePassword(String email) {
        return Observable.create(subscriber -> {
            subscriber.onStart();

            ParseUser.requestPasswordResetInBackground(email, e -> {
                if (e == null) {
                    subscriber.onCompleted();
                } else {
                    subscriber.onError(e);
                }
            });
        });
    }

    public Observable<ParseUser> loginWithParse(String username, String password) {
        return ParseObservable.logIn(username, password);
    }

    public Observable<Void> logoutWithParse() {
        return ParseObservable.logOut();
    }

    public Observable<ParseUser> signupWithParse(String username, String password, String name) {
        ParseUser user = new ParseUser();
        user.setUsername(username);
        user.setPassword(password);
        user.put(USER_NAME_FIELD, name);

        return Observable.create(subscriber -> {
            subscriber.onStart();

            user.signUpInBackground(e -> {
                if (e == null) {
                    subscriber.onNext(ParseUser.getCurrentUser());
                    subscriber.onCompleted();
                } else {
                    subscriber.onError(e);
                }
            });
        });
    }

    public Observable<ParseUser> loginWithFacebook(ActivityBase activityBase) {
        return Observable.create(new Observable.OnSubscribe<ParseUser>() {
            @Override
            public void call(Subscriber<? super ParseUser> subscriber) {
                ParseFacebookUtils.logInWithReadPermissionsInBackground(
                        activityBase,
                        Arrays.asList("public_profile"),
                        new LogInCallback() {
                            @Override
                            public void done(ParseUser user, ParseException e) {
                                if (e == null) {
                                    subscriber.onCompleted();
                                } else {
                                    subscriber.onError(e);
                                }
                            }
                        });
            }
        });
    }

    public Observable<ParseUser> loginWithTwitter(ActivityBase activityBase) {
        return Observable.create(new Observable.OnSubscribe<ParseUser>() {
            @Override
            public void call(Subscriber<? super ParseUser> subscriber) {
                ParseTwitterUtils.logIn(activityBase, new LogInCallback() {
                    @Override
                    public void done(ParseUser user, ParseException e) {
                        Logg.log("DONE");
                        if (user != null && e != null) {
                            subscriber.onError(e);
                        } else {
                            subscriber.onCompleted();
                        }
                    }
                });
            }
        });
    }

    public static void handleError(ParseException e, View view, ActivityBase activityBase) {
        view = view == null ? activityBase.getWindow().getDecorView() : view;
        switch (e.getCode()) {
            case ParseException.INVALID_SESSION_TOKEN:
                activityBase.showSnackbar(view,
                                R.string.parse_error_invalid_session,
                                Snackbar.LENGTH_INDEFINITE,
                                new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        activityBase.startLoginActivity(ActivityBase.REQUEST_AUTHENTICATION_CODE);
                                    }
                                });
                break;
            case ParseException.USERNAME_TAKEN:
                activityBase.showSnackbar(view,
                        R.string.parse_error_username_taken,
                        Snackbar.LENGTH_SHORT, null);
                break;
            default:
                Logg.log(e.getCode() + ", " + e.getLocalizedMessage());
                activityBase.showSnackbar(view, R.string.parse_error_unknown,
                        Snackbar.LENGTH_SHORT,
                        null);
                break;
        }
    }
}
