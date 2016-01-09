package milespeele.canvas.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.widget.FrameLayout;

import com.facebook.login.widget.LoginButton;
import com.parse.ParseUser;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterLoginButton;

import butterknife.Bind;
import butterknife.ButterKnife;
import milespeele.canvas.R;
import milespeele.canvas.fragment.FragmentLogin;
import rx.Subscriber;

/**
 * Created by mbpeele on 1/9/16.
 */
public class ActivityAuthenticate extends ActivityBase implements FragmentLogin.FragmentLoginListener {

    public static Intent newIntent(Context context) {
        return new Intent(context, ActivityAuthenticate.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authenticate);

        getFragmentManager()
                .beginTransaction()
                .add(R.id.activity_authentiate_fragment_frame, FragmentLogin.newInstance())
                .commit();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onParseLoginClicked(String username, String password) {
        Subscriber<ParseUser> parseUserSubscriber = new Subscriber<ParseUser>() {
            @Override
            public void onCompleted() {
                removeSubscription(this);
                setResult(RESULT_OK);
                finish();
            }

            @Override
            public void onError(Throwable e) {
                showSnackbar("Auth error", Snackbar.LENGTH_SHORT, e);
            }

            @Override
            public void onNext(ParseUser parseUser) {

            }
        };

        addSubscription(parseUtils.login(username, password).subscribe(parseUserSubscriber));
    }

    @Override
    public void onFacebookLoginClicked(LoginButton loginButton) {
//        loginButton.registerCallback();
    }

    @Override
    public void onTwitterLoginClicked(TwitterLoginButton loginButton) {
//        loginButton.setCallback(new Callback<TwitterSession>() {
//            @Override
//            public void success(Result<TwitterSession> result) {
//                // The TwitterSession is also available through:
//                // Twitter.getInstance().core.getSessionManager().getActiveSession()
////                TwitterSession session = result.data;
//                // TODO: Remove toast and use the TwitterSession's userID
//                // with your app's user model
////                String msg = "@" + session.getUserName() + " logged in! (#" + session.getUserId() + ")";
////                Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
//            }
//            @Override
//            public void failure(TwitterException exception) {
////                Log.d("TwitterKit", "Login with Twitter failure", exception);
//            }
//        });
    }

    @Override
    public void onSignupClicked() {
        // switch fragments
    }
}
