package milespeele.canvas.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import milespeele.canvas.MainApp;
import milespeele.canvas.R;
import milespeele.canvas.parse.Masterpiece;

/**
 * Created by Miles Peele on 7/12/2015.
 */
public class FragmentMasterpiece extends Fragment {

    @InjectView(R.id.fragment_gallery_photo) Target view;

    @Inject Picasso picasso;

    private static Masterpiece masterpiece;

    public FragmentMasterpiece() {}

    public static FragmentMasterpiece newInstance(Masterpiece masterpiece) {
        FragmentMasterpiece.masterpiece = masterpiece;
        return new FragmentMasterpiece();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ((MainApp) activity.getApplicationContext()).getApplicationComponent().inject(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_masterpiece, container, false);
        ButterKnife.inject(this, v);
        picasso.load(masterpiece.getImage().getUrl())
                .placeholder(R.drawable.rotate)
                .into(view);
        return v;
    }

}
