package milespeele.canvas.fragment;


import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.Fragment;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.location.Location;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPropertyAnimatorListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.BounceInterpolator;
import android.widget.LinearLayout;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import milespeele.canvas.R;
import milespeele.canvas.util.Logger;
import milespeele.canvas.view.ViewCanvas;
import milespeele.canvas.view.ViewFabDrawerBehavior;

public class FragmentDrawer extends Fragment implements View.OnClickListener {

    @InjectView(R.id.fragment_drawer_canvas) ViewCanvas drawer;
    @InjectView(R.id.fragment_drawer_fab) FloatingActionButton menuToggle;
    @InjectView(R.id.fragment_drawer_coordinator) CoordinatorLayout parent;
    @InjectView(R.id.fragment_drawer_palette) LinearLayout palette;

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
        return v;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
    }

    @Override
    @OnClick({R.id.fragment_drawer_fab, R.id.palette_color, R.id.palette_brush_size,
            R.id.palette_trash, R.id.palette_undo, R.id.palette_redo})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fragment_drawer_fab:
                showOrHidePalette();
                break;
            case R.id.palette_color:
                listener.showColorPicker();
                break;
            case R.id.palette_brush_size:
                listener.showWidthPicker();
                break;
            case R.id.palette_trash:
                drawer.clearCanvas();
                break;
            case R.id.palette_undo:
                drawer.undo();
                break;
            case R.id.palette_redo:
                drawer.redo();
                break;
        }
    }

    private void showOrHidePalette() {
        if (!isPaletteVisible) {
            isPaletteVisible = true;
            animateIn();
            rotateToShowMenuOpen();
        } else {
            isPaletteVisible = false;
            rotateToShowMenuClosed();
            animateOut();
        }
    }

    private void rotateToShowMenuOpen() {
        ObjectAnimator imageViewObjectAnimator = ObjectAnimator.ofFloat(menuToggle,
                "rotation", 0f, 180f);
        imageViewObjectAnimator.start();
    }

    private void rotateToShowMenuClosed() {
        ObjectAnimator imageViewObjectAnimator = ObjectAnimator.ofFloat(menuToggle,
                "rotation", 180f, 360f);
        imageViewObjectAnimator.start();
    }

    private void animateIn() {
        ViewCompat.animate(palette).scaleX(1.0F).scaleY(1.0F).alpha(1.0F)
                .setInterpolator(INTERPOLATOR)
                .withLayer()
                .start();
    }

    private void animateOut() {
        ViewCompat.animate(palette).scaleX(0).scaleY(0).alpha(0)
                .setInterpolator(INTERPOLATOR)
                .withLayer()
                .start();
    }

    public Bitmap giveBitmapToActivity() {
        return drawer.getBitmap();
    }

    public void changeColor(int color) {
        drawer.changeColor(color);
    }
}
