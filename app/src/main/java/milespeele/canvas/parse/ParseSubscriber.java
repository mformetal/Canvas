package milespeele.canvas.parse;

import android.view.View;

import com.parse.Parse;
import com.parse.ParseException;

import java.lang.ref.SoftReference;
import java.lang.reflect.Type;

import milespeele.canvas.activity.ActivityBase;
import milespeele.canvas.util.Logg;
import rx.Observable;
import rx.Subscriber;

/**
 * Created by mbpeele on 1/10/16.
 */
public abstract class ParseSubscriber<T> extends Subscriber<T> {

    private SoftReference<ActivityBase> activityBaseSoftReference;
    private SoftReference<View> viewSoftReference;

    public ParseSubscriber(ActivityBase activityBase, View view) {
        super();
        activityBaseSoftReference = new SoftReference<>(activityBase);
        viewSoftReference = new SoftReference<>(view);
        activityBase.addSubscription(this);
    }

    public ParseSubscriber(ActivityBase activityBase) {
        super();
        activityBaseSoftReference = new SoftReference<>(activityBase);
        viewSoftReference = new SoftReference<>(activityBase.getWindow().getDecorView());
        activityBase.addSubscription(this);
    }

    @Override
    public void onError(Throwable e) {
        removeSelf();

        if (e instanceof ParseException) {
            ActivityBase activityBase = activityBaseSoftReference.get();
            View view = viewSoftReference.get();

            if (view != null && activityBase != null) {
                ParseUtils.handleError((ParseException) e, view, activityBase);
            }
        } else {
            Logg.log("ERROR NOT FROM PARSE", e);
        }
    }

    @Override
    public void onCompleted() {
        removeSelf();
    }

    private void removeSelf() {
        ActivityBase activityBase = activityBaseSoftReference.get();
        if (activityBase != null) {
            activityBase.removeSubscription(this);
        }
    }
}
