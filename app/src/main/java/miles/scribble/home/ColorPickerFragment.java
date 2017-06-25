package miles.scribble.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.ButterKnife;
import miles.scribble.R;
import miles.scribble.ui.BaseFragment;

public class ColorPickerFragment extends BaseFragment
        implements View.OnClickListener {

    @BindView(R.id.fragment_color_picker_title) TextView title;

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

        title.setTextColor(currentColor);
        return v;
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fragment_color_picker_select:
            case R.id.fragment_color_picker_cancel:
                getActivity().onBackPressed();
                break;
        }
    }
}