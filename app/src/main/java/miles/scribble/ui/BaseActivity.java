package miles.scribble.ui;

import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.annotation.StringRes;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import io.realm.Realm;
import rx.Subscription;
import rx.subscriptions.CompositeSubscription;

import java.util.ArrayList;

/**
 * Created by milespeele on 7/14/15.
 */
public abstract class BaseActivity extends AppCompatActivity {

    public Realm realm;

    private CompositeSubscription mCompositeSubscription;
    private ArrayList<Subscription> mRemovableSubscriptions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        realm = Realm.getDefaultInstance();

        mRemovableSubscriptions = new ArrayList<>();

        mCompositeSubscription = new CompositeSubscription();
    }

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        realm.close();
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
