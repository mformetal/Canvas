package milespeele.canvas.activity;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;

import milespeele.canvas.R;

/**
 * Created by milespeele on 7/14/15.
 */
public abstract class ActivityBase extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
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
}
