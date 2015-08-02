package milespeele.canvas.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import butterknife.ButterKnife;
import butterknife.InjectView;
import milespeele.canvas.R;
import milespeele.canvas.view.ViewBottomSheet;
import milespeele.canvas.view.ViewBottomSheetMenu;
import milespeele.canvas.view.ViewCanvas;

public class FragmentDrawer extends Fragment implements ViewBottomSheetMenu.FabMenuListener {

    @InjectView(R.id.fragment_drawer_canvas) ViewCanvas drawer;
    @InjectView(R.id.fragment_drawer_bottom_sheet) ViewBottomSheet parent;
    @InjectView(R.id.fragment_drawer_eraser) ImageView eraser;
    @InjectView(R.id.fragment_drawer_ink) ImageView ink;

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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_drawer, container, false);
        ButterKnife.inject(this, v);
        parent.inflateMenu(this);
        return v;
    }


    public Bitmap giveBitmapToActivity() {
        return drawer.getBitmap();
    }

    public void changeColor(int color) {
        parent.dismissSheet();
        drawer.changeColor(color);
    }

    public void fillCanvas(int color) {
        parent.dismissSheet();
        drawer.fillCanvas(color);
    }

    public void setBrushWidth(float width) {
        parent.dismissSheet();
        drawer.setBrushWidth(width);
    }

    @Override
    public void onColorizeClicked() {
        parent.dismissSheet();
        drawer.showInk(ink);
    }

    @Override
    public void onEraseClicked() {
        parent.dismissSheet();
        drawer.changeToEraser(eraser);
    }

    @Override
    public void onPaintColorClicked(int viewId) {
        listener.showColorPicker(viewId);
    }

    @Override
    public void onBrushClicked() {
        listener.showBrushPicker(drawer.getBrushWidth());
    }

    @Override
    public void onUndoClicked() {
        drawer.undo();
    }

    @Override
    public void onRedoClicked() {
        drawer.redo();
    }
}
