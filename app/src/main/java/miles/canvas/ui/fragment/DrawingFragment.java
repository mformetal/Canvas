package miles.canvas.ui.fragment;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import butterknife.Bind;
import butterknife.ButterKnife;
import miles.canvas.R;
import miles.canvas.ui.activity.HomeActivity;
import miles.canvas.ui.widget.CanvasLayout;

public class DrawingFragment extends BaseFragment {

    @Bind(R.id.fragment_drawer_coordinator) CanvasLayout coordinatorLayout;

    public DrawingFragment() {}

    public static DrawingFragment newInstance() {
        return new DrawingFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_drawer, container, false);
        ButterKnife.bind(this, v);
        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        coordinatorLayout.setActivityListener((HomeActivity) getActivity());
    }

    public CanvasLayout getRootView() {
        return coordinatorLayout;
    }

    public Bitmap getDrawingBitmap() {
        return coordinatorLayout.getDrawerBitmap();
    }
}
