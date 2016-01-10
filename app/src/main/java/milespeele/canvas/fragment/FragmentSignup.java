package milespeele.canvas.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.Objects;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import milespeele.canvas.R;
import milespeele.canvas.util.TextUtils;
import milespeele.canvas.view.ViewTypefaceButton;
import milespeele.canvas.view.ViewTypefaceEditText;

/**
 * Created by mbpeele on 1/9/16.
 */
public class FragmentSignup extends FragmentBase implements View.OnClickListener {

    @Bind(R.id.fragment_login_username_input) ViewTypefaceEditText usernameInput;
    @Bind(R.id.fragment_login_password_input) ViewTypefaceEditText passwordInput;
    @Bind(R.id.fragment_signup_password_confirm) ViewTypefaceEditText confirmPasswordInput;
    @Bind(R.id.fragment_signup_name) ViewTypefaceEditText profileNameInput;
    @Bind(R.id.fragment_signup_create_account) ViewTypefaceButton signupCreate;

    private FragmentSignupListener mListener;

    public FragmentSignup() {}

    public static FragmentSignup newInstance() {
        return new FragmentSignup();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mListener = (FragmentSignupListener) context;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_signup, container, false);
        ButterKnife.bind(this, v);

        usernameInput.setText("mbpeele@email.wm.edu");
        passwordInput.setText("test");
        confirmPasswordInput.setText("test");
        profileNameInput.setText("name");
        return v;
    }

    @Override
    @OnClick(R.id.fragment_signup_create_account)
    public void onClick(View v) {
        String email = usernameInput.getTextAsString();
        String password = passwordInput.getTextAsString();
        String confirmPassword = confirmPasswordInput.getTextAsString();
        String name = profileNameInput.getTextAsString();

        if (validateUsername(email)
                && validatePassword(password, confirmPassword)) {
            mListener.onSignupCreate(email, password, name);
        }
    }

    private boolean validateUsername(String string) {
        if (string.length() == 0) {
            usernameInput.setError("Email must not be empty");
            return false;
        }

        if (!TextUtils.validateEmail(string)) {
            usernameInput.setError("Email must be valid");
            return false;
        }

        return true;
    }

    private boolean validatePassword(String string, String other) {
        if (!Objects.equals(string, other)) {
            passwordInput.setError("Passwords much match");
            return false;
        }

        if (string.length() < 4) {
            passwordInput.setError("Password must be greater than 4 characters");
            return false;
        }

        return true;
    }

    public interface FragmentSignupListener {

        void onSignupCreate(String email, String password, String name);

    }
}
