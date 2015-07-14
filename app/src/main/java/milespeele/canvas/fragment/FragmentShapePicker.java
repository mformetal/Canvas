package milespeele.canvas.fragment;


import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import butterknife.ButterKnife;
import milespeele.canvas.R;


public class FragmentShapePicker extends DialogFragment {

    public static FragmentShapePicker newInstance() {
        return new FragmentShapePicker();
    }

    public FragmentShapePicker() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_shape_picker, container, false);
        ButterKnife.inject(this, v);
        return v;
    }


}
