package milespeele.canvas.fragment;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;
import milespeele.canvas.MainApp;
import milespeele.canvas.R;
import milespeele.canvas.event.EventBrushSizeChosen;
import milespeele.canvas.view.ViewBrushPicker;
import milespeele.canvas.view.ViewBrushSize;

/**
 * Created by milespeele on 7/13/15.
 */
public class FragmentBrushPicker extends DialogFragment
        implements View.OnClickListener {

    @Bind(R.id.fragment_brush_picker_view) ViewBrushPicker root;

    @Inject EventBus bus;

    private static final String THICKNESS = "thick";

    public FragmentBrushPicker() {}

    public static FragmentBrushPicker newInstance(float canvasWidth) {
        FragmentBrushPicker fragmentBrushPicker = new FragmentBrushPicker();
        Bundle args = new Bundle();
        args.putFloat(THICKNESS, canvasWidth);
        fragmentBrushPicker.setArguments(args);
        return fragmentBrushPicker;
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
        return dialog;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_brush_picker, container, false);
        ButterKnife.bind(this, v);
        root.setThickness(getArguments().getFloat(THICKNESS));
        return v;
    }

    @Override
    @OnClick({R.id.fragment_brush_picker_pos, R.id.fragment_brush_picker_cancel})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fragment_brush_picker_pos:
                if (root.getThickness() != getArguments().getFloat(THICKNESS)) {
                    bus.post(new EventBrushSizeChosen(root.getThickness()));
                } else {
                    bus.post(new EventBrushSizeChosen(0));
                }
                break;
            case R.id.fragment_brush_picker_cancel:
                bus.post(new EventBrushSizeChosen(0));
                break;
        }
        dismiss();
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
}
