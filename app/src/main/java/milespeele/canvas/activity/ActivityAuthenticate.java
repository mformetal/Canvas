package milespeele.canvas.activity;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.transition.Transition;
import android.transition.TransitionInflater;

import com.facebook.login.widget.LoginButton;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.twitter.sdk.android.core.identity.TwitterLoginButton;

import milespeele.canvas.R;
import milespeele.canvas.fragment.FragmentLogin;
import milespeele.canvas.fragment.FragmentSignup;
import milespeele.canvas.parse.ParseSubscriber;
import milespeele.canvas.parse.ParseUtils;
import milespeele.canvas.util.Logg;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.parse.ParseObservable;
import rx.schedulers.Schedulers;

/**
 * Created by mbpeele on 1/9/16.
 */
public class ActivityAuthenticate extends ActivityBase
    implements FragmentLogin.FragmentLoginListener, FragmentSignup.FragmentSignupListener {

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
    public void onBackPressed() {
        int count = getFragmentManager().getBackStackEntryCount();
        if (count == 0) {
            super.onBackPressed();
        } else {
            getFragmentManager().popBackStack();
        }
    }

    @Override
    public void onParseLoginClicked(String username, String password) {
        if (hasInternet()) {
            if (!mProgressDialog.isShowing()) {
                ParseSubscriber<ParseUser> subscriber = new ParseSubscriber<ParseUser>(this) {
                    @Override
                    public void onCompleted() {
                        super.onCompleted();
                        dismissLoading();

                        setResult(RESULT_OK);
                        finish();
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
    public void onFacebookLoginClicked() {

    }

    @Override
    public void onTwitterLoginClicked() {

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
                .addSharedElement(login.usernameInput, "email")
                .addSharedElement(login.passwordInput, "password")
                .commit();
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

                        setResult(RESULT_OK);
                        finish();
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
