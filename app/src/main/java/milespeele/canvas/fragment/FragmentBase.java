package milespeele.canvas.fragment;

import android.app.Fragment;
import android.content.Context;

import javax.inject.Inject;

import butterknife.ButterKnife;
import de.greenrobot.event.EventBus;
import milespeele.canvas.MainApp;

/**
 * Created by mbpeele on 1/3/16.
 */
public class FragmentBase extends Fragment {

    @Inject EventBus bus;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        ((MainApp) context.getApplicationContext()).getApplicationComponent().inject(this);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }
}
