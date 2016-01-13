package miles.canvas.ui.fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.larswerkman.holocolorpicker.ColorPicker;
import com.larswerkman.holocolorpicker.OpacityBar;
import com.larswerkman.holocolorpicker.SVBar;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import miles.canvas.R;
import miles.canvas.data.event.EventColorChosen;
import miles.canvas.ui.widget.TypefaceTextView;

public class ColorPickerFragment extends BaseFragment
        implements View.OnClickListener, ColorPicker.OnColorChangedListener {

    @Bind(R.id.fragment_color_picker_title)
    TypefaceTextView title;
    @Bind(R.id.fragment_color_picker_view) ColorPicker picker;
    @Bind(R.id.fragment_color_picker_sv) SVBar svBar;
    @Bind(R.id.fragment_color_picker_opacity) OpacityBar opacityBar;

    private int currentColor;

    public ColorPickerFragment() {}

    public static ColorPickerFragment newInstance(int previousColor, boolean fillCanvas) {
        ColorPickerFragment fragment = new ColorPickerFragment();
        Bundle args = new Bundle();
        args.putInt("prev", previousColor);
        args.putBoolean("toFill", fillCanvas);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_color_picker, container, false);
        ButterKnife.bind(this, v);

        currentColor = getArguments().getInt("prev");

        picker.setColor(Color.rgb(Color.red(currentColor), Color.green(currentColor), Color.blue(currentColor)));
        picker.setShowOldCenterColor(false);
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
    @OnClick({R.id.fragment_color_picker_cancel, R.id.fragment_color_picker_select})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fragment_color_picker_select:
                bus.post(new EventColorChosen(picker.getColor(),
                        getArguments().getBoolean("toFill")));
            case R.id.fragment_color_picker_cancel:
                getActivity().onBackPressed();
                break;
        }
    }

    @Override
    public void onColorChanged(int color) {
        currentColor = color;
        title.setTextColor(currentColor);
    }
}