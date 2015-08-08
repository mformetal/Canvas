package milespeele.canvas.fragment;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;
import milespeele.canvas.MainApp;
import milespeele.canvas.R;
import milespeele.canvas.event.EventColorChosen;
import milespeele.canvas.view.ViewColorPicker;

/**
 * Created by milespeele on 7/3/15.
 */
public class FragmentColorPicker extends DialogFragment
    implements View.OnClickListener {

    @Bind(R.id.fragment_color_picker_which) TextView which;
    @Bind(R.id.fragment_color_picker_view) ViewColorPicker picker;
    @Inject EventBus bus;

    private final static String TAG = "which";

    public FragmentColorPicker(){}

    public static FragmentColorPicker newInstance(String whichColor) {
        FragmentColorPicker fragment = new FragmentColorPicker();
        Bundle args = new Bundle();
        args.putString(TAG, whichColor);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ((MainApp) activity.getApplicationContext()).getApplicationComponent().inject(this);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setCanceledOnTouchOutside(true);
        return dialog;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_color_picker, container, false);
        ButterKnife.bind(this, v);
        which.setText(getArguments().getString(TAG));
        return v;
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
    @OnClick({R.id.fragment_color_picker_cancel, R.id.fragment_color_picker_select})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fragment_color_picker_select:
                if (picker.getColor() == -1) {
                    bus.post(new EventColorChosen(Color.WHITE, getArguments().getString(TAG)));
                } else {
                    bus.post(new EventColorChosen(picker.getColor(), getArguments().getString(TAG)));
                }
                break;
            case R.id.fragment_color_picker_cancel:
                bus.post(new EventColorChosen(0, getArguments().getString(TAG)));
                break;
        }
        dismiss();
    }
}
