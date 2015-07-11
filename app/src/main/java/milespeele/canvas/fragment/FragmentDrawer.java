package milespeele.canvas.fragment;


import android.app.Activity;
import android.app.Fragment;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.view.ViewCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import milespeele.canvas.R;
import milespeele.canvas.view.ViewCanvas;
import milespeele.canvas.view.ViewFabMenu;

public class FragmentDrawer extends Fragment implements ViewFabMenu.FabMenuListener {

    @InjectView(R.id.fragment_drawer_canvas) ViewCanvas drawer;
    @InjectView(R.id.fragment_drawer_coordinator) CoordinatorLayout parent;
    @InjectView(R.id.fragment_drawer_palette) ViewFabMenu palette;

    private final static AccelerateDecelerateInterpolator INTERPOLATOR = new AccelerateDecelerateInterpolator();
    private static boolean isPaletteVisible = true;

    private FragmentListener listener;

    public FragmentDrawer() {}

    public static FragmentDrawer newInstance() {
        return new FragmentDrawer();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        listener = (FragmentListener) activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_drawer, container, false);
        ButterKnife.inject(this, v);
        palette.setListener(this);
        return v;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
    }

    @Override
    public void onColorClicked() {
        listener.showColorPicker();
    }

    @Override
    public void onWidthClicked() {
        // TODO
    }

    @Override
    public void onClearClicked() {
        drawer.clearCanvas();
    }

    @Override
    public void onUndoClicked() {
        drawer.undo();
    }

    @Override
    public void onRedoClicked() {
        drawer.redo();
    }

    public Bitmap giveBitmapToActivity() {
        return drawer.getBitmap();
    }

    public void changeColor(int color) {
        drawer.changeColor(color);
    }
}
