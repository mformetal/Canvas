package milespeele.canvas.fragment;

import android.animation.Animator;
import android.app.Fragment;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import butterknife.Bind;
import butterknife.ButterKnife;
import milespeele.canvas.R;
import milespeele.canvas.view.ViewCanvasLayout;

public class FragmentDrawer extends Fragment {

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
        return v;
    }

    @Override
    public Animator onCreateAnimator(int transit, boolean enter, int nextAnim) {
        return (enter) ? coordinatorLayout.reveal(getArguments().getFloat("x", 0),
                getArguments().getFloat("y", 0)) : coordinatorLayout.unreveal();
    }

    public void unreveal() {
        coordinatorLayout.unreveal();
    }

    public Bitmap giveBitmapToActivity() {
        return coordinatorLayout.getDrawerBitmap();
    }
}
