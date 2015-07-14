package milespeele.canvas.fragment;


import android.app.Activity;
import android.app.Fragment;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import butterknife.ButterKnife;
import butterknife.InjectView;
import milespeele.canvas.R;
import milespeele.canvas.util.Logger;
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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
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
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putParcelable("bitmap", drawer.getBitmap());
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            Logger.log("HAS BITMAP");
        }
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
    public void onBrushClicked() { listener.showBrushPicker(drawer.getBrushWidth()); }

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

    public void setBrushWidth(float width) { drawer.setBrushWidth(width); }
}
