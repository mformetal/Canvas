package milespeele.canvas.activity;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;

import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.parse.ParseUser;
import com.parse.ui.ParseLoginBuilder;

import javax.inject.Inject;

import de.greenrobot.event.EventBus;
import milespeele.canvas.MainApp;
import milespeele.canvas.R;
import milespeele.canvas.parse.ParseUtils;
import milespeele.canvas.util.Logg;
import rx.Subscription;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by milespeele on 7/14/15.
 */
public abstract class ActivityBase extends Activity {

    @Inject ParseUtils parseUtils;
    @Inject EventBus bus;

    private CompositeSubscription mCompositeSubscription;

    public int REQUEST_AUTHENTICATION_CODE = 2004;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((MainApp) getApplication()).getApplicationComponent().inject(this);

        FacebookSdk.sdkInitialize(getApplicationContext());

        mCompositeSubscription = new CompositeSubscription();
    }

    @Override
    protected void onResume() {
        super.onResume();
        AppEventsLogger.activateApp(this, getResources().getString(R.string.facebook_id));
    }

    @Override
    protected void onPause() {
        super.onPause();
        AppEventsLogger.deactivateApp(this, getResources().getString(R.string.facebook_id));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mCompositeSubscription.hasSubscriptions()) {
            mCompositeSubscription.unsubscribe();
        }
    }

    public void addSubscription(Subscription subscription) {
        mCompositeSubscription.add(subscription);
    }

    public void removeSubscription(Subscription subscription) {
        mCompositeSubscription.remove(subscription);
    }

    public boolean checkPermission(String permission) {
        int hasPermission = ContextCompat.checkSelfPermission(this, permission);
        return hasPermission == PackageManager.PERMISSION_GRANTED;
    }

    public boolean checkPermissions(String[] permissions) {
        for (String permission: permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }

        return true;
    }

    public void checkUser() {
        ParseLoginBuilder builder = new ParseLoginBuilder(this);
        startActivityForResult(builder.build(), 0);

//        if (ParseUser.getCurrentUser() == null) {
//            startActivityForResult(ActivityAuthenticate.newIntent(this), REQUEST_AUTHENTICATION_CODE);
//        }
    }

    public void showSnackbar(String string, int duration, Throwable e) {
        Logg.log(getClass().getName(), e);
        Snackbar.make(getWindow().getDecorView(), string, duration).show();
    }
}
