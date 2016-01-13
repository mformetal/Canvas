package miles.canvas.rx;

import java.lang.ref.SoftReference;

import miles.canvas.ui.activity.BaseActivity;
import miles.canvas.util.Logg;
import rx.Subscriber;

/**
 * Created by mbpeele on 1/10/16.
 */
public abstract class SafeSubscription<T> extends Subscriber<T> {

    private SoftReference<BaseActivity> activityBaseSoftReference;

    public SafeSubscription(BaseActivity baseActivity) {
        super();
        activityBaseSoftReference = new SoftReference<>(baseActivity);
        baseActivity.addSubscription(this);
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
        BaseActivity baseActivity = activityBaseSoftReference.get();
        if (baseActivity != null) {
            baseActivity.removeSubscription(this);
        }
    }

    public BaseActivity getActivity() {
        return activityBaseSoftReference.get();
    }
}
