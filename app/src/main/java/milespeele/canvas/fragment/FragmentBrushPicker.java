package milespeele.canvas.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.graphics.Paint;
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
import milespeele.canvas.event.EventBrushChosen;
import milespeele.canvas.view.ViewBrushPickerLayout;

/**
 * Created by milespeele on 7/13/15.
 */
public class FragmentBrushPicker extends Fragment implements View.OnClickListener {

    @Bind(R.id.fragment_brush_picker_view) ViewBrushPickerLayout root;

    @Inject EventBus bus;

    private static Paint curPaint = new Paint();

    public FragmentBrushPicker() {}

    public static FragmentBrushPicker newInstance(Paint paint) {
        curPaint.set(paint);
        return new FragmentBrushPicker();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ((MainApp) activity.getApplicationContext()).getApplicationComponent().inject(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_brush_picker, container, false);
        ButterKnife.bind(this, v);
        root.setInitialValues(curPaint);
        return v;
    }

    @Override
    @OnClick({R.id.fragment_brush_picker_pos, R.id.fragment_brush_picker_cancel})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fragment_brush_picker_pos:
                bus.post(new EventBrushChosen(root.getLastSelectedPaint()));
            case R.id.fragment_brush_picker_cancel:
                getActivity().onBackPressed();
                break;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }
}
