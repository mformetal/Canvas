package milespeele.canvas.fragment;


import android.app.Activity;
import android.app.Fragment;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import milespeele.canvas.R;
import milespeele.canvas.view.ViewCanvas;

public class FragmentDrawer extends Fragment implements View.OnClickListener {

    @InjectView(R.id.fragment_drawer_canvas) ViewCanvas drawer;
    @InjectView(R.id.fragment_drawer_fab) FloatingActionButton fab;
    @InjectView(R.id.fragment_drawer_coordinator) CoordinatorLayout parent;

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
    @OnClick(R.id.fragment_drawer_fab)
    public void onClick(View v) {
    }

    public void clearCanvas() {
        drawer.clearCanvas();
    }

    public void startErasing() {
        //drawer.changeToEraser();
    }

    public Bitmap giveBitmapToActivity() {
        return drawer.getBitmap();
    }

    public void changeColor(int color) {
        drawer.changeColor(color);
    }

    public void undo() {
        drawer.undo();
    }
}
