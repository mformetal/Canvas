package milespeele.canvas.fragment;


import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import butterknife.ButterKnife;
import butterknife.InjectView;
import milespeele.canvas.R;
import milespeele.canvas.view.ViewCanvas;

public class FragmentDrawer extends Fragment {

    @InjectView(R.id.fragment_drawer_canvas) ViewCanvas drawer;

    public FragmentDrawer() {}

    public static FragmentDrawer newInstance() {
        return new FragmentDrawer();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_drawer, container, false);
        ButterKnife.inject(this, v);
        return v;
    }

    public void clearCanvas() {
        drawer.clearCanvas();
    }

    public void startErasing() {
        //drawer.changeToEraser();
    }

    public void changeColor(int color) {
        drawer.changeColor(color);
    }

    public void undo() {
        //drawer.undo();
    }

}
