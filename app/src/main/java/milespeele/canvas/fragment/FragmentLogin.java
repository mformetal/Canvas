package milespeele.canvas.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.facebook.AccessToken;
import com.facebook.login.widget.LoginButton;
import com.parse.ParseUser;
import com.twitter.sdk.android.core.identity.TwitterLoginButton;

import java.util.Objects;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import milespeele.canvas.R;
import milespeele.canvas.parse.ParseUtils;
import milespeele.canvas.util.Logg;
import milespeele.canvas.view.ViewTypefaceButton;
import milespeele.canvas.view.ViewTypefaceEditText;
import milespeele.canvas.view.ViewTypefaceTextView;

/**
 * Created by mbpeele on 1/9/16.
 */
public class FragmentLogin extends FragmentBase implements View.OnClickListener {

    public @Bind(R.id.fragment_login_title) ViewTypefaceTextView appLogo;
    public @Bind(R.id.fragment_login_username_input) ViewTypefaceEditText usernameInput;
    public @Bind(R.id.fragment_login_password_input) ViewTypefaceEditText passwordInput;
    @Bind(R.id.fragment_login_fb_login) ViewTypefaceButton facebookButton;
    @Bind(R.id.fragment_login_twitter_login) ViewTypefaceButton twitterButton;
    @Bind(R.id.fragment_login_parse_login) ViewTypefaceButton parseLoginButton;
    @Bind(R.id.fragment_login_parse_signup) ViewTypefaceButton parseSignupButton;
    @Bind(R.id.fragment_login_help) ViewTypefaceButton parseResetButton;

    private FragmentLoginListener mListener;

    public FragmentLogin() {}

    public static FragmentLogin newInstance() {
        return new FragmentLogin();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mListener = (FragmentLoginListener) context;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_login, container, false);
        ButterKnife.bind(this, v);


        if (ParseUtils.isParseUserAvailable()) {
            parseLoginButton.setText(R.string.parse_login_logout_label);
        }

        facebookButton.setVisibility(View.GONE);
        twitterButton.setVisibility(View.GONE);

        usernameInput.setText("mbpeele@email.wm.edu");
        passwordInput.setText("test");
        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
//        mListener.onLoginAvailable(facebookButton, twitterButton);
    }

    @Override
    @OnClick({R.id.fragment_login_parse_login, R.id.fragment_login_parse_signup,
                R.id.fragment_login_help, R.id.fragment_login_fb_login,
            R.id.fragment_login_twitter_login})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fragment_login_parse_login:
                String username = usernameInput.getTextAsString();
                String password = passwordInput.getTextAsString();
                if (validateUsername(username) && validatePassword(password)) {
                    mListener.onParseLoginClicked(username, password);
                }
                break;
            case R.id.fragment_login_parse_signup:
                mListener.onSignupClicked();
                break;
            case R.id.fragment_login_help:
                String email = usernameInput.getTextAsString();
                if (validateUsername(email)) {
                    mListener.onResetPasswordClicked(email);
                }
                break;
            case R.id.fragment_login_fb_login:
                mListener.onFacebookClicked();
                break;
            case R.id.fragment_login_twitter_login:
                mListener.onTwitterClicked();
                break;
        }
    }

    private boolean validateUsername(String string) {
        if (string.length() == 0) {
            usernameInput.setError("Username must not be empty");
            return false;
        }

        return true;
    }

    private boolean validatePassword(String string) {
        if (string.length() == 0) {
            passwordInput.setError("Password must not be empty");
            return false;
        }

        return true;
    }

    public interface FragmentLoginListener {

        void onParseLoginClicked(String username, String password);

        void onSignupClicked();

        void onResetPasswordClicked(String email);

        void onFacebookClicked();

        void onTwitterClicked();
    }
}