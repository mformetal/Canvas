package milespeele.canvas.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;
import milespeele.canvas.MainApp;
import milespeele.canvas.R;
import milespeele.canvas.event.EventFilenameChosen;
import milespeele.canvas.event.EventTextChosen;
import milespeele.canvas.view.ViewTypefaceEditText;

/**
 * Created by mbpeele on 11/14/15.
 */
public class FragmentText extends Fragment implements View.OnClickListener {

    @Bind(R.id.fragment_text_input) ViewTypefaceEditText input;

    @Inject EventBus bus;

    public FragmentText() {}

    public static FragmentText newInstance() {
        return new FragmentText();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_text, container, false);
        ButterKnife.bind(this, v);
        return v;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ((MainApp) activity.getApplicationContext()).getApplicationComponent().inject(this);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

    @Override
    @OnClick({R.id.fragment_text_pos_button, R.id.fragment_text_neg_button})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fragment_text_pos_button:
                bus.post(new EventTextChosen(input.getTextAsString()));
                break;
            case R.id.fragment_text_neg_button:
                break;
        }
        getActivity().onBackPressed();
    }
}
