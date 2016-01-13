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
import miles.canvas.ui.activity.HomeActivity;
import miles.canvas.ui.drawing.DrawingCurve;
import miles.canvas.ui.widget.CanvasLayout;
import miles.canvas.ui.widget.Fab;
import miles.canvas.ui.widget.FabMenu;
import miles.canvas.ui.widget.OptionsMenu;
import miles.canvas.ui.widget.CanvasLayout;
import miles.canvas.ui.widget.Fab;
import miles.canvas.ui.widget.FabMenu;
import miles.canvas.ui.widget.OptionsMenu;

public class DrawingFragment extends BaseFragment implements
        FabMenu.ViewFabMenuListener, OptionsMenu.ViewOptionsMenuListener {

    @Bind(R.id.fragment_drawer_coordinator)
    CanvasLayout coordinatorLayout;

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
        coordinatorLayout.setMenuListeners(this);
        return v;
    }

    @Override
    public void onFabMenuButtonClicked(Fab v) {
        HomeActivity activityHome = (HomeActivity) getActivity();
        if (activityHome != null) {
            activityHome.onFabMenuButtonClicked(v);
        }
    }

    @Override
    public void onOptionsMenuCancel() {}

    @Override
    public void onOptionsMenuButtonClicked(View view, DrawingCurve.State state) {
        HomeActivity activityHome = (HomeActivity) getActivity();
        if (activityHome != null) {
            activityHome.onOptionsMenuClicked(view, state);
        }
    }

    @Override
    public void onOptionsMenuAccept() {}

    public CanvasLayout getRootView() {
        return coordinatorLayout;
    }

    public Bitmap getDrawingBitmap() {
        return coordinatorLayout.getDrawerBitmap();
    }
}
