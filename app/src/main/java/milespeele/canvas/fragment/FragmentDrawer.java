package milespeele.canvas.fragment;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.Fragment;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.flipboard.bottomsheet.BottomSheetLayout;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import milespeele.canvas.R;
import milespeele.canvas.view.ViewCanvas;
import milespeele.canvas.view.ViewFab;
import milespeele.canvas.view.ViewFabMenu;



public class FragmentDrawer extends Fragment implements View.OnClickListener, ViewFabMenu.FabMenuListener {

    @InjectView(R.id.fragment_drawer_canvas) ViewCanvas drawer;
    @InjectView(R.id.fragment_drawer_coordinator) CoordinatorLayout parent;
    @InjectView(R.id.fragment_drawer_bottom_sheet) BottomSheetLayout bottomSheetLayout;
    @InjectView(R.id.fragment_drawer_show_menu) ViewFab toggle;
    private ViewFabMenu menu;

    private ObjectAnimator rotateOpen;
    private ObjectAnimator rotateClose;

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
        rotateOpen = ObjectAnimator.ofFloat(toggle, "rotation", 0f, 135f);
        rotateClose = ObjectAnimator.ofFloat(toggle, "rotation", 135f, 270f);

        inflateMenu();
        return v;
    }

    @Override
    @OnClick(R.id.fragment_drawer_show_menu)
    public void onClick(View v) {
        if (bottomSheetLayout.isSheetShowing()) {
            bottomSheetLayout.dismissSheet();
            rotateClose.start();
        } else {
            if (menu == null) {
                inflateMenu();
            }
            bottomSheetLayout.showWithSheetView(menu);
            rotateOpen.start();
        }
    }

    private void inflateMenu() {
        menu = new ViewFabMenu(getActivity());
        LayoutInflater.from(getActivity()).inflate(R.layout.fab_menu, menu, true);
        menu.setListener(this);
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

    public void setBrushWidth(float width) { drawer.setBrushWidth(width); }

    @Override
    public void onColorizeClicked() {

    }

    @Override
    public void onEraseClicked() {
        drawer.changeToEraser();
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
}
