package miles.canvas.fragment;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import butterknife.Bind;
import butterknife.ButterKnife;
import miles.canvas.R;
import miles.canvas.activity.ActivityHome;
import miles.canvas.drawing.DrawingCurve;
import miles.canvas.view.ViewCanvasLayout;
import miles.canvas.view.ViewFab;
import miles.canvas.view.ViewFabMenu;
import miles.canvas.view.ViewOptionsMenu;

public class FragmentDrawer extends FragmentBase implements
        ViewFabMenu.ViewFabMenuListener, ViewOptionsMenu.ViewOptionsMenuListener {

    @Bind(R.id.fragment_drawer_coordinator) ViewCanvasLayout coordinatorLayout;

    public FragmentDrawer() {}

    public static FragmentDrawer newInstance() {
        return new FragmentDrawer();
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
    public void onFabMenuButtonClicked(ViewFab v) {
        ActivityHome activityHome = (ActivityHome) getActivity();
        if (activityHome != null) {
            activityHome.onFabMenuButtonClicked(v);
        }
    }

    @Override
    public void onOptionsMenuCancel() {}

    @Override
    public void onOptionsMenuButtonClicked(View view, DrawingCurve.State state) {
        ActivityHome activityHome = (ActivityHome) getActivity();
        if (activityHome != null) {
            activityHome.onOptionsMenuClicked(view, state);
        }
    }

    @Override
    public void onOptionsMenuAccept() {}

    public ViewCanvasLayout getRootView() {
        return coordinatorLayout;
    }

    public Bitmap getDrawingBitmap() {
        return coordinatorLayout.getDrawerBitmap();
    }
}
