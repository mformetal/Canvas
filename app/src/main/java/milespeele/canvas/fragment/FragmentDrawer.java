package milespeele.canvas.fragment;


import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.Fragment;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.view.ViewCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.BounceInterpolator;

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
    public void onShapeClicked() {
        listener.showShapePicker();
    }

    @Override
    public void onPaintColorClicked(int viewId) {
        listener.showColorPicker(viewId);
    }

    @Override
    public void onWidthClicked() {
        // TODO
    }

    @Override
    public void onUndoClicked() {
        drawer.undo();
    }

    @Override
    public void onRedoClicked() {
        drawer.redo();
    }

    @Override
    public void onFillClicked(int viewId) {
        listener.showColorPicker(viewId);
    }

    public Bitmap giveBitmapToActivity() {
        return drawer.getBitmap();
    }

    public void changeColor(int color) {
        drawer.changeColor(color);
    }

    public void fillCanvas(int color) {
        drawer.fillCanvas(color);
    }

    public void eraseCanvas() { drawer.clearCanvas(); }
}
