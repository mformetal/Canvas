package milespeele.canvas.fragment;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;
import milespeele.canvas.MainApp;
import milespeele.canvas.R;
import milespeele.canvas.event.EventFilenameChosen;

/**
 * Created by Miles Peele on 7/13/2015.
 */
public class FragmentFilename extends DialogFragment implements View.OnClickListener {

    @Bind(R.id.fragment_filename_input) EditText input;

    @Inject EventBus bus;

    public FragmentFilename(){}

    public static FragmentFilename newInstance() {
        return new FragmentFilename();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_filename, container, false);
        ButterKnife.bind(this, v);
        return v;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ((MainApp) activity.getApplicationContext()).getApplicationComponent().inject(this);
    }

    @Override
    public void onActivityCreated(Bundle arg0) {
        super.onActivityCreated(arg0);
        getDialog().getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

    @Override
    @OnClick({R.id.fragment_filename_pos_button, R.id.fragment_filename_neg_button})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fragment_filename_pos_button:
                bus.post(new EventFilenameChosen(input.getText().toString()));
                break;
            case R.id.fragment_filename_neg_button:
                bus.post(new EventFilenameChosen(""));
                break;
        }
        dismiss();
    }
}
