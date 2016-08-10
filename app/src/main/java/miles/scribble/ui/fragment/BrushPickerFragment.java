package miles.scribble.ui.fragment;

import android.graphics.Paint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import miles.scribble.R;
import miles.scribble.data.event.EventBrushChosen;
import miles.scribble.ui.widget.BrushPickerLayout;

/**
 * Created by milespeele on 7/13/15.
 */
public class BrushPickerFragment extends BaseFragment implements View.OnClickListener {

    BrushPickerLayout root;

    private static final Paint curPaint = new Paint();

    public BrushPickerFragment() {}

    public static BrushPickerFragment newInstance(Paint paint) {
        curPaint.set(paint);
        return new BrushPickerFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_brush_picker, container, false);
        root.setPaint(curPaint);
        return v;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fragment_brush_picker_pos:
                bus.post(new EventBrushChosen(root.getPaint()));
            case R.id.fragment_brush_picker_cancel:
                getActivity().onBackPressed();
                break;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }
}