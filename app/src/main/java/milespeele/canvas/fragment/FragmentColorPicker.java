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
import milespeele.canvas.view.ViewColorPicker;

/**
 * Created by milespeele on 7/3/15.
 */
public class FragmentColorPicker extends Fragment implements View.OnClickListener, ViewColorPicker.ViewColorPickerListener {

    @Bind(R.id.fragment_color_picker_view) ViewColorPicker picker;

    @Inject EventBus bus;

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

        currentColor = getArguments().getInt(PREV);
        picker.setCurrentColor(currentColor);
        picker.setListener(this);
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
//                bus.post(new EventColorChosen(picker.getColor(), opacityBar.getOpacity(),
//                        toggle.isChecked()));
                break;
            case R.id.fragment_color_picker_cancel:
        }
        getActivity().onBackPressed();
    }

    @Override
    public void onColorChanged(int newColor) {
        currentColor = newColor;
    }
}
