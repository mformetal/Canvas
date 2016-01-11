package milespeele.canvas.activity;

import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.transition.Transition;
import android.transition.TransitionInflater;
import android.view.View;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterLoginButton;

import java.util.Arrays;

import milespeele.canvas.R;
import milespeele.canvas.fragment.FragmentLogin;
import milespeele.canvas.fragment.FragmentSignup;
import milespeele.canvas.parse.ParseSubscriber;
import milespeele.canvas.parse.ParseUtils;
import milespeele.canvas.util.Logg;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by mbpeele on 1/9/16.
 */
public class ActivityAuthenticate extends ActivityBase
    implements FragmentLogin.FragmentLoginListener, FragmentSignup.FragmentSignupListener,
        View.OnClickListener {

    public static Intent newIntent(Context context) {
        return new Intent(context, ActivityAuthenticate.class);
    }

    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authenticate);

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage(getResources().getString(R.string.parse_login_loading_dialog));

        getFragmentManager()
                .beginTransaction()
                .add(R.id.activity_authenticate_fragment_frame, FragmentLogin.newInstance())
                .commit();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        ParseFacebookUtils.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onBackPressed() {
        if (mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
            removeLastSubscription();
            return;
        }

        int count = getFragmentManager().getBackStackEntryCount();
        if (count == 0) {
            super.onBackPressed();
        } else {
            getFragmentManager().popBackStack();
        }
    }

    @Override
    public void onClick(View v) {}

    @Override
    @SuppressWarnings("unchecked")
    public void onParseLoginClicked(String username, String password) {
        if (hasInternet()) {
            ParseSubscriber subscriber = new ParseSubscriber(this) {
                @Override
                public void onCompleted() {
                    super.onCompleted();

                    dismissLoading();

                    onActivitySuccess();
                }

                @Override
                public void onError(Throwable e) {
                    dismissLoading();
                    handleAuthenticationError(e);
                }

                @Override
                public void onStart() {
                    showLoading();
                }
            };

            if (ParseUtils.isParseUserAvailable()) {
                parseUtils.logoutWithParse()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(subscriber);
            } else {
                parseUtils.loginWithParse(username, password)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(subscriber);
            }
        } else {
            showSnackbar(R.string.snackbar_no_internet, Snackbar.LENGTH_SHORT, null);
        }
    }

    @Override
    public void onSignupClicked() {
        FragmentManager manager = getFragmentManager();
        FragmentLogin login = (FragmentLogin)
                manager.findFragmentById(R.id.activity_authenticate_fragment_frame);
        FragmentSignup signup = FragmentSignup.newInstance();

        Transition changeBounds =
                TransitionInflater.from(this).inflateTransition(R.transition.fragment_authenticate_transition);
        Transition explodeTransform = TransitionInflater.from(this).
                inflateTransition(android.R.transition.explode);

        login.setSharedElementReturnTransition(changeBounds);
        login.setExitTransition(explodeTransform);

        signup.setSharedElementEnterTransition(changeBounds);
        signup.setEnterTransition(explodeTransform);

        getFragmentManager().beginTransaction()
                .replace(R.id.activity_authenticate_fragment_frame, signup)
                .addToBackStack("transaction")
                .addSharedElement(login.appLogo, "title")
                .addSharedElement(login.usernameInput, "email")
                .addSharedElement(login.passwordInput, "password")
                .commit();
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onResetPasswordClicked(String email) {
        if (hasInternet()) {
            ParseSubscriber parseSubscriber = new ParseSubscriber(this) {
                @Override
                public void onCompleted() {
                    super.onCompleted();

                    dismissLoading();

                    FragmentLogin fragmentLogin = (FragmentLogin) getFragmentManager()
                            .findFragmentById(R.id.activity_authenticate_fragment_frame);
                    if (fragmentLogin != null) {
                        if (getActivity() != null) {
                            showSnackbar(R.string.parse_login_forgot_password_sent,
                                    Snackbar.LENGTH_INDEFINITE, new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            onActivitySuccess();
                                        }
                                    });
                        }
                    }
                }

                @Override
                public void onError(Throwable e) {
                    super.onError(e);
                    dismissLoading();
                }

                @Override
                public void onStart() {
                    super.onStart();
                    showLoading();
                }
            };

             parseUtils.resetParsePassword(email)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(parseSubscriber);
        } else {
            showSnackbar(R.string.snackbar_no_internet, Snackbar.LENGTH_SHORT, null);
        }
    }

    @Override
    public void onFacebookClicked() {
        if (hasInternet()) {
            ParseSubscriber<ParseUser> subscriber = new ParseSubscriber<ParseUser>(this) {
                @Override
                public void onCompleted() {
                    super.onCompleted();
                    onActivitySuccess();
                }
            };

            parseUtils.loginWithFacebook(this)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(subscriber);
        } else {
            showSnackbar(R.string.snackbar_no_internet, Snackbar.LENGTH_SHORT, null);
        }
    }

    @Override
    public void onTwitterClicked() {
        if (hasInternet()) {
            ParseSubscriber<ParseUser> subscriber = new ParseSubscriber<ParseUser>(this) {
                @Override
                public void onCompleted() {
                    super.onCompleted();
                    onActivitySuccess();
                }
            };

            parseUtils.loginWithTwitter(this)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(subscriber);
        } else {
            showSnackbar(R.string.snackbar_no_internet, Snackbar.LENGTH_SHORT, null);
        }
    }

    @Override
    public void onSignupCreate(String email, String password, String name) {
        if (hasInternet()) {
            ParseSubscriber<ParseUser> parseSubscriber = new ParseSubscriber<ParseUser>(this) {
                @Override
                public void onCompleted() {
                    super.onCompleted();

                    onActivitySuccess();
                }

                @Override
                public void onError(Throwable e) {
                    super.onError(e);
                    dismissLoading();
                }

                @Override
                public void onStart() {
                    super.onStart();
                    showLoading();
                }
            };

             parseUtils.signupWithParse(email, password, name)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(parseSubscriber);
        } else {
            showSnackbar(R.string.snackbar_no_internet, Snackbar.LENGTH_SHORT, null);
        }
    }

    private void showLoading() {
        mProgressDialog.show();
    }

    private void dismissLoading() {
        mProgressDialog.dismiss();
    }

    private void onActivitySuccess() {
        Logg.log("SUCCESS");
        setResult(RESULT_OK);
        finish();
    }

    private void handleAuthenticationError(Throwable throwable) {
        if (throwable instanceof ParseException) {
            switch (((ParseException) throwable).getCode()) {
                case ParseException.OBJECT_NOT_FOUND:
                    showSnackbar(R.string.parse_error_invalid_creds,
                            Snackbar.LENGTH_LONG, null);
                    break;
                default:
                    Logg.log(((ParseException) throwable).getCode() + ", " +
                            throwable.getLocalizedMessage());
                    ParseUtils.handleError((ParseException) throwable, null, this);
                    break;
            }
        }
    }
}
