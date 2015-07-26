package milespeele.canvas.fragment;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.support.v7.app.AppCompatDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import milespeele.canvas.R;
import milespeele.canvas.view.ViewBrushSize;

/**
 * Created by milespeele on 7/13/15.
 */
public class FragmentBrushPicker extends DialogFragment
        implements View.OnClickListener, OnSeekBarChangeListener {

    @InjectView(R.id.fragment_brush_picker_reveal_layout) RelativeLayout container;
    @InjectView(R.id.fragment_brush_picker_seek) SeekBar seek;
    @InjectView(R.id.fragment_brush_picker_changes) ViewBrushSize line;

    private int thickness = 0;
    private static final String THICKNESS = "thick";

    private FragmentListener mListener;

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
        mListener = (FragmentListener) activity;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = new AppCompatDialog(getActivity(), R.style.DialogTheme);
        dialog.getWindow().setTitle(getResources().getString(R.string.fragment_brush_picker_title));
        dialog.setCanceledOnTouchOutside(true);
        return dialog;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_brush_picker, container, false);
        ButterKnife.inject(this, v);
        line.onThicknessChanged(Math.round(getArguments().getFloat(THICKNESS)));
        seek.setProgress(Math.round(getArguments().getFloat(THICKNESS)));
        seek.setOnSeekBarChangeListener(this);
        return v;
    }

    @Override
    @OnClick({R.id.fragment_brush_picker_pos, R.id.fragment_brush_picker_cancel})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fragment_brush_picker_pos:
                if (thickness != getArguments().getFloat(THICKNESS)) {
                    mListener.onBrushSizeChosen(thickness);
                } else {
                    mListener.onBrushSizeChosen(0);
                }
                break;
            case R.id.fragment_brush_picker_cancel:
                mListener.onBrushSizeChosen(0);
                break;
        }
    }

    @Override
    public void onActivityCreated(Bundle arg0) {
        super.onActivityCreated(arg0);
        getDialog().getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        thickness = progress;
        line.onThicknessChanged(progress);
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }
}
