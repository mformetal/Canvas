package milespeele.canvas.fragment;

import android.animation.Animator;
import android.app.Activity;
import android.app.Fragment;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.common.eventbus.EventBus;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import milespeele.canvas.MainApp;
import milespeele.canvas.R;
import milespeele.canvas.activity.ActivityHome;
import milespeele.canvas.util.BitmapUtils;
import milespeele.canvas.view.ViewCanvasLayout;
import milespeele.canvas.view.ViewFab;
import milespeele.canvas.view.ViewFabMenu;

public class FragmentDrawer extends Fragment implements ViewFabMenu.ViewFabMenuListener {

    @Bind(R.id.fragment_drawer_coordinator) ViewCanvasLayout coordinatorLayout;

    public FragmentDrawer() {}

    public static FragmentDrawer newInstance(float cx, float cy) {
        FragmentDrawer drawer = new FragmentDrawer();
        Bundle bundle = new Bundle();
        bundle.putFloat("x", cx);
        bundle.putFloat("y", cy);
        drawer.setArguments(bundle);
        return drawer;
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
        coordinatorLayout.setMenuListener(this);
        return v;
    }

    @Override
    public Animator onCreateAnimator(int transit, boolean enter, int nextAnim) {
        return (enter) ? coordinatorLayout.reveal(getArguments().getFloat("x", 0f),
                getArguments().getFloat("y", 0f)) : coordinatorLayout.unreveal();
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
