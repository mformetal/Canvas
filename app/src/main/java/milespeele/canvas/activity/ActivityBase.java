package milespeele.canvas.activity;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.annotation.StringRes;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.parse.ParseUser;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

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
    @Inject Picasso picasso;

    private CompositeSubscription mCompositeSubscription;
    private ArrayList<Subscription> mRemovableSubscriptions;

    public final static int REQUEST_AUTHENTICATION_CODE = 2004;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((MainApp) getApplication()).getApplicationComponent().inject(this);

        mRemovableSubscriptions = new ArrayList<>();

        mCompositeSubscription = new CompositeSubscription();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mCompositeSubscription.hasSubscriptions()) {
            mCompositeSubscription.unsubscribe();
        }
    }

    public void addSubscription(Subscription subscription) {
        mRemovableSubscriptions.add(subscription);
        mCompositeSubscription.add(subscription);
    }

    public void removeSubscription(Subscription subscription) {
        mCompositeSubscription.remove(subscription);
        mRemovableSubscriptions.remove(subscription);
    }

    public void removeLastSubscription() {
        if (!mRemovableSubscriptions.isEmpty()) {
            Subscription subscription = mRemovableSubscriptions.get(mRemovableSubscriptions.size() - 1);
            mCompositeSubscription.remove(subscription);
        }
    }

    public boolean hasInternet() {
        ConnectivityManager manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        return manager.getActiveNetworkInfo() != null && manager.getActiveNetworkInfo().isConnectedOrConnecting();
    }

    public boolean checkPermissions(String[] permissions) {
        for (String permission: permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }

        return true;
    }

    public void startLoginActivity(int code) {
        startActivityForResult(ActivityAuthenticate.newIntent(this), code);
    }

    public void showSnackbar(@StringRes int res, int duration, View.OnClickListener onClickListener) {
        showSnackbar(null, res, duration, onClickListener);
    }

    public void showSnackbar(View view, @StringRes int res, int duration, View.OnClickListener onClickListener) {
        Snackbar snackbar = Snackbar.make(view == null ? getWindow().getDecorView() : view, res, duration);
        if (onClickListener != null) {
            snackbar.setAction("Aight", onClickListener);
        }
        snackbar.show();
    }
}
