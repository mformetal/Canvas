package milespeele.canvas.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.larswerkman.holocolorpicker.ColorPicker;
import com.larswerkman.holocolorpicker.OpacityBar;
import com.larswerkman.holocolorpicker.SVBar;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;
import milespeele.canvas.MainApp;
import milespeele.canvas.R;
import milespeele.canvas.event.EventColorChosen;
import milespeele.canvas.util.AbstractAnimatorListener;
import milespeele.canvas.util.Logg;
import milespeele.canvas.view.ViewTypefaceButton;
import milespeele.canvas.view.ViewTypefaceTextView;



public class FragmentColorPicker extends Fragment
        implements View.OnClickListener, ColorPicker.OnColorChangedListener {

    @Bind(R.id.fragment_color_picker_title) ViewTypefaceTextView title;
    @Bind(R.id.fragment_color_picker_view) ColorPicker picker;
    @Bind(R.id.fragment_color_picker_sv) SVBar svBar;
    @Bind(R.id.fragment_color_picker_opacity) OpacityBar opacityBar;
    @Bind(R.id.fragment_color_picker_canvas) ViewTypefaceButton canvasToggle;
    @Bind(R.id.fragment_color_picker_stroke) ViewTypefaceButton strokeToggle;

    @Inject EventBus bus;

    private boolean forStroke = true;
    private int currentColor;
    private final static String PREV = "prev";

    public FragmentColorPicker() {}

    public static FragmentColorPicker newInstance(int previousColor) {
        FragmentColorPicker fragment = new FragmentColorPicker();
        Bundle args = new Bundle();
        args.putInt(PREV, previousColor);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ((MainApp) activity.getApplicationContext()).getApplicationComponent().inject(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_color_picker, container, false);
        ButterKnife.bind(this, v);

        strokeToggle.setBackgroundTintList(getResources().getColorStateList(R.color.spirit_gold));

        currentColor = getArguments().getInt(PREV);

        picker.setShowOldCenterColor(false);
        picker.setColor(getArguments().getInt(PREV));
        picker.addSVBar(svBar);
        picker.addOpacityBar(opacityBar);
        picker.setOnColorChangedListener(this);

        title.setTextColor(currentColor);
        return v;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

    @Override
    @OnClick({R.id.fragment_color_picker_cancel, R.id.fragment_color_picker_select,
            R.id.fragment_color_picker_stroke, R.id.fragment_color_picker_canvas})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fragment_color_picker_select:
                if (currentColor == getArguments().getInt(PREV)) {
                    bus.post(new EventColorChosen(picker.getColor(), forStroke));
                } else {
                    bus.post(new EventColorChosen(currentColor, forStroke));
                }
            case R.id.fragment_color_picker_cancel:
                getActivity().onBackPressed();
                break;

            case R.id.fragment_color_picker_stroke:
                forStroke = true;
                strokeToggle.setBackgroundTintList(getResources().getColorStateList(R.color.spirit_gold));
                canvasToggle.setBackgroundTintList(getResources().getColorStateList(android.R.color.white));
                break;
            case R.id.fragment_color_picker_canvas:
                forStroke = false;
                canvasToggle.setBackgroundTintList(getResources().getColorStateList(R.color.spirit_gold));
                strokeToggle.setBackgroundTintList(getResources().getColorStateList(android.R.color.white));
                break;
        }
    }

    @Override
    public void onColorChanged(int color) {
        currentColor = color;
        title.setTextColor(currentColor);
    }
}
