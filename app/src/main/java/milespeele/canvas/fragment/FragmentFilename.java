package milespeele.canvas.fragment;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.support.v7.app.AppCompatDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import milespeele.canvas.R;

/**
 * Created by Miles Peele on 7/13/2015.
 */
public class FragmentFilename extends DialogFragment implements View.OnClickListener {

    @InjectView(R.id.fragment_filename_input) EditText input;

    private FragmentListener mListener;

    public FragmentFilename(){}

    public static FragmentFilename newInstance() {
        return new FragmentFilename();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = new AppCompatDialog(getActivity(), R.style.DialogTheme);
        dialog.getWindow().setTitle(getResources().getString(R.string.fragment_filename_title));
        dialog.setCanceledOnTouchOutside(true);
        return dialog;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_filename, container, false);
        ButterKnife.inject(this, v);
        return v;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mListener = (FragmentListener) activity;
    }

    @Override
    public void onActivityCreated(Bundle arg0) {
        super.onActivityCreated(arg0);
        getDialog().getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
    }

    @Override
    @OnClick({R.id.fragment_filename_pos_button, R.id.fragment_filename_neg_button})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fragment_filename_pos_button:
                mListener.onFilenameChosen(input.getText().toString());
                break;
            case R.id.fragment_filename_neg_button:
                mListener.onFilenameChosen("");
                break;
        }
    }
}
