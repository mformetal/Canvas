package miles.canvas.util;

import java.lang.ref.SoftReference;

import miles.canvas.activity.ActivityBase;
import rx.Subscriber;

/**
 * Created by mbpeele on 1/10/16.
 */
public abstract class SafeSubscription<T> extends Subscriber<T> {

    private SoftReference<ActivityBase> activityBaseSoftReference;

    public SafeSubscription(ActivityBase activityBase) {
        super();
        activityBaseSoftReference = new SoftReference<>(activityBase);
        activityBase.addSubscription(this);
    }

    @Override
    public void onError(Throwable e) {
        removeSelf();
        Logg.log(e);
    }

    @Override
    public void onNext(T t) {

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

    public ActivityBase getActivity() {
        return activityBaseSoftReference.get();
    }
}
