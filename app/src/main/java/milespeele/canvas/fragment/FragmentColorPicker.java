package milespeele.canvas.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.graphics.Color;
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
import milespeele.canvas.event.EventColorChosen;
import milespeele.canvas.util.AbstractAnimatorListener;
import milespeele.canvas.util.Logg;
import milespeele.canvas.view.ViewColorPicker;
import milespeele.canvas.view.ViewTypefaceButton;
import milespeele.canvas.view.ViewTypefaceTextView;



public class FragmentColorPicker extends Fragment implements View.OnClickListener {

    @Bind(R.id.fragment_color_picker_view) ViewColorPicker picker;
    @Bind(R.id.fragment_color_picker_pos_button) ViewTypefaceButton posButton;
    @Bind(R.id.fragment_color_picker_neg_button) ViewTypefaceButton negButton;

    @Inject EventBus bus;

    public FragmentColorPicker() {}

    public static FragmentColorPicker newInstance(int previousColor) {
        FragmentColorPicker fragment = new FragmentColorPicker();
        Bundle args = new Bundle();
        args.putInt("prev", previousColor);
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
        picker.setTitleColor(getArguments().getInt("prev"));
        return v;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

    @Override
    @OnClick({R.id.fragment_color_picker_neg_button, R.id.fragment_color_picker_pos_button})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fragment_color_picker_pos_button:
                bus.post(new EventColorChosen(picker.getSelectedColor()));
            case R.id.fragment_color_picker_neg_button:
                getActivity().onBackPressed();
                break;
        }
    }

}
