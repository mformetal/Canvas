package miles.scribble.ui.fragment;

import android.app.Fragment;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;

import org.greenrobot.eventbus.EventBus;

import javax.inject.Inject;

import miles.scribble.MainApp;

/**
 * Created by mbpeele on 1/3/16.
 */
public class BaseFragment extends Fragment {

    public @Inject
    EventBus bus;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        ((MainApp) context.getApplicationContext()).getApplicationComponent().inject(this);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @NonNull
    @Override
    public View getView() {
        return super.getView();
    }

    public boolean onBackPressed() {
        return false;
    }
}
