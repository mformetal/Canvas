package milespeele.canvas.fragment;

import android.app.Fragment;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import butterknife.Bind;
import butterknife.ButterKnife;
import milespeele.canvas.R;
import milespeele.canvas.activity.ActivityHome;
import milespeele.canvas.view.ViewCanvasLayout;
import milespeele.canvas.view.ViewFab;
import milespeele.canvas.view.ViewFabMenu;
import milespeele.canvas.view.ViewOptionsMenu;

public class FragmentDrawer extends Fragment implements ViewFabMenu.ViewFabMenuListener {

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
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onFabMenuButtonClicked(ViewFab v) {
        ActivityHome activityHome = (ActivityHome) getActivity();
        if (activityHome != null) {
            activityHome.onFabMenuButtonClicked(v);
        }
    }

    public ViewCanvasLayout getRootView() { return coordinatorLayout; }

    public Bitmap getDrawingBitmap() {
        return coordinatorLayout.getDrawerBitmap();
    }
}
