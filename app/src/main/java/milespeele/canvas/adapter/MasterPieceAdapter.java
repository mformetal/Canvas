package milespeele.canvas.adapter;

import android.app.Fragment;
import android.app.FragmentManager;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v13.app.FragmentStatePagerAdapter;

import java.util.List;

import milespeele.canvas.fragment.FragmentMasterpiece;
import milespeele.canvas.parse.Masterpiece;
import milespeele.canvas.util.Logger;

/**
 * Created by Miles Peele on 7/12/2015.
 */
public class MasterpieceAdapter extends FragmentStatePagerAdapter {

    private static List<Masterpiece> masterpieces;

    public MasterpieceAdapter(FragmentManager mgr, List<Masterpiece> masterpieceList) {
        super(mgr);
        masterpieces = masterpieceList;
    }

    @Override
    public Fragment getItem(int position) {
        return FragmentMasterpiece.newInstance(masterpieces.get(position));
    }

    @Override
    public int getCount() {
        return masterpieces.size() - 1;
    }
}
