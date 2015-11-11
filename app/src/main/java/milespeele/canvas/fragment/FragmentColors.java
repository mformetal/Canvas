package milespeele.canvas.fragment;

import android.app.Fragment;
import android.os.Bundle;

import java.util.ArrayList;

/**
 * Created by mbpeele on 11/10/15.
 */
public class FragmentColors extends Fragment {


    public FragmentColors() { }

    public static FragmentColors newInstance(ArrayList<Integer> colors) {
        FragmentColors fragmentColors = new FragmentColors();
        Bundle args = new Bundle();
        args.putIntegerArrayList("colors", colors);
        fragmentColors.setArguments(args);
        return fragmentColors;
    }
}
