package milespeele.canvas.activity;

import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.transition.Transition;
import android.transition.TransitionInflater;
import android.view.View;
import android.widget.Toast;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterLoginButton;

import milespeele.canvas.R;
import milespeele.canvas.fragment.FragmentLogin;
import milespeele.canvas.fragment.FragmentSignup;
import milespeele.canvas.parse.ParseSubscriber;
import milespeele.canvas.parse.ParseUtils;
import milespeele.canvas.util.Logg;
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
    private CallbackManager mCallbackManager;
    private TwitterLoginButton mTwitterLoginButton;
    private LoginButton mFacebookLoginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authenticate);

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage(getResources().getString(R.string.parse_login_loading_dialog));

        mCallbackManager = CallbackManager.Factory.create();

        getFragmentManager()
                .beginTransaction()
                .add(R.id.activity_authenticate_fragment_frame, FragmentLogin.newInstance())
                .commit();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mCallbackManager.onActivityResult(requestCode, resultCode, data);
        mTwitterLoginButton.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onBackPressed() {
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
    public void onParseLoginClicked(String username, String password) {
        if (hasInternet()) {
            if (!mProgressDialog.isShowing()) {
                ParseSubscriber<ParseUser> subscriber = new ParseSubscriber<ParseUser>(this) {
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
                    public void onNext(ParseUser o) {

                    }

                    @Override
                    public void onStart() {
                        showLoading();
                    }
                };

                parseUtils.login(username, password)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(subscriber);
            }
        } else {
            showSnackbar(R.string.snackbar_no_internet, Snackbar.LENGTH_SHORT, null);
        }
    }

    @Override
    public void onParseLogoutClicked() {
        if (hasInternet()) {
            ParseSubscriber<Void> parseSubscriber = new ParseSubscriber<Void>(this) {
                @Override
                public void onCompleted() {
                    super.onCompleted();
                    dismissLoading();
                    onActivitySuccess();
                }

                @Override
                public void onNext(Void aVoid) {

                }

                @Override
                public void onStart() {
                    super.onStart();
                    showLoading();
                }
            };

            parseUtils.logout()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(parseSubscriber);
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
                public void onNext(Object o) {

                }

                @Override
                public void onStart() {
                    super.onStart();
                    showLoading();
                }
            };

            parseUtils.resetPassword(email)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(parseSubscriber);
        } else {
            showSnackbar(R.string.snackbar_no_internet, Snackbar.LENGTH_SHORT, null);
        }
    }

    @Override
    public void onLoginAvailable(LoginButton loginButton, TwitterLoginButton twitterLoginButton) {
        mFacebookLoginButton = loginButton;
        mTwitterLoginButton = twitterLoginButton;

        if (isApplicationInstalled("com.twitter.android")) {
            mTwitterLoginButton.setCallback(twitterCallback);
            mTwitterLoginButton.setOnClickListener(this);
        } else {
            mTwitterLoginButton.setVisibility(View.GONE);
        }

        if (isApplicationInstalled("com.facebook.katana")) {
            mFacebookLoginButton.registerCallback(mCallbackManager, facebookCallback);
            mFacebookLoginButton.setOnClickListener(this);
        } else {
            mFacebookLoginButton.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onSignupCreate(String email, String password, String name) {
        if (hasInternet()) {
            if (!mProgressDialog.isShowing()) {
                ParseSubscriber<ParseUser> parseSubscriber = new ParseSubscriber<ParseUser>(this) {
                    @Override
                    public void onCompleted() {
                        super.onCompleted();
                        dismissLoading();

                        onActivitySuccess();
                    }

                    @Override
                    public void onNext(ParseUser parseUser) {
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

                parseUtils.signup(email, password, name)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(parseSubscriber);
            }
        } else {
            showSnackbar(R.string.snackbar_no_internet, Snackbar.LENGTH_SHORT, null);
        }
    }

    private void showLoading() {
        if (!mProgressDialog.isShowing()) {
            mProgressDialog.show();
        }
    }

    private void dismissLoading() {
        if (mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }

    private void onActivitySuccess() {
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

    private final FacebookCallback<LoginResult> facebookCallback = new FacebookCallback<LoginResult>() {
        @Override
        public void onSuccess(LoginResult loginResult) {
            onActivitySuccess();
        }

        @Override
        public void onCancel() {
            Logg.log("FACEBOOK CANCEL");
        }

        @Override
        public void onError(FacebookException error) {
            Logg.log("FACEBOOK ERROR", error);
        }
    };

    private final Callback<TwitterSession> twitterCallback = new Callback<TwitterSession>() {
        @Override
        public void success(Result<TwitterSession> result) {
            Logg.log("TWITTER SUCCESS");
        }

        @Override
        public void failure(TwitterException e) {
            showSnackbar(R.string.pasre_login_twitter_error, Snackbar.LENGTH_SHORT, null);
        }
    };
}
