package milespeele.canvas.fragment;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import butterknife.Bind;
import butterknife.ButterKnife;
import milespeele.canvas.R;
import milespeele.canvas.view.ViewCanvasLayout;
import milespeele.canvas.view.ViewDashboard;

/**
 * Created by mbpeele on 10/19/15.
 */
public class FragmentDashboard extends Fragment {

    @Bind(R.id.fragment_dashboard_root) ViewDashboard viewDashboard;

    public FragmentDashboard() {}

    public static FragmentDashboard newInstance() {
        return new FragmentDashboard();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_dashboard, container, false);
        ButterKnife.bind(this, v);
        return v;
    }

//    @Override
//    public Animator onCreateAnimator(int transit, boolean enter, int nextAnim) {
//        if (enter) {
//            return super.onCreateAnimator(transit, enter, nextAnim);
//        } else {
//            ObjectAnimator anim = ObjectAnimator.ofFloat(this, "alpha", 1, 0);
//            anim.setDuration(300 << 2);
//            return anim;
//        }
//    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }
}
