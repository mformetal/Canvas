package milespeele.canvas.activity;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.transition.Transition;
import android.transition.TransitionInflater;

import com.facebook.login.widget.LoginButton;
import com.parse.ParseUser;
import com.twitter.sdk.android.core.identity.TwitterLoginButton;

import milespeele.canvas.R;
import milespeele.canvas.fragment.FragmentLogin;
import milespeele.canvas.fragment.FragmentSignup;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authenticate);

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
        ParseObservable.logIn(username, password)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<ParseUser>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                    }

                    @Override
                    public void onNext(ParseUser parseUser) {

                    }
                });
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

        // Setup exit transition on first fragment
        login.setSharedElementReturnTransition(changeBounds);
        login.setExitTransition(explodeTransform);

        // Setup enter transition on second fragment
        signup.setSharedElementEnterTransition(changeBounds);
        signup.setEnterTransition(explodeTransform);

        // Add second fragment by replacing first
        getFragmentManager().beginTransaction()
                .replace(R.id.activity_authenticate_fragment_frame, signup)
                .addToBackStack("transaction")
                .addSharedElement(login.usernameInput, "email")
                .addSharedElement(login.passwordInput, "password")
                .commit();

//        manager.beginTransaction()
//                .replace(R.id.activity_authenticate_fragment_frame, FragmentSignup.newInstance())
//                .addToBackStack(null)
//                .commit();
    }

    @Override
    public void onSignupCreate() {

    }
}
