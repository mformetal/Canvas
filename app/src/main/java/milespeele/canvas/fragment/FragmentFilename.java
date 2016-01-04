package milespeele.canvas.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import milespeele.canvas.R;
import milespeele.canvas.event.EventFilenameChosen;
import milespeele.canvas.view.ViewTypefaceEditText;

/**
 * Created by Miles Peele on 7/13/2015.
 */
public class FragmentFilename extends FragmentBase implements View.OnClickListener {

    @Bind(R.id.fragment_filename_input) ViewTypefaceEditText input;

    public FragmentFilename(){}

    public static FragmentFilename newInstance() {
        return new FragmentFilename();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_filename, container, false);
        ButterKnife.bind(this, v);
        return v;
    }

    @Override
    @OnClick({R.id.fragment_filename_pos_button, R.id.fragment_filename_neg_button})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fragment_filename_pos_button:
                String name = input.getTextAsString();
                if (name.isEmpty()) {
                    Toast.makeText(getActivity(),
                            getResources().getString(R.string.fragment_filename_empty),
                            Toast.LENGTH_SHORT).show();
                } else {
                    bus.post(new EventFilenameChosen(input.getTextAsString()));
                    getActivity().onBackPressed();
                }
                break;
            case R.id.fragment_filename_neg_button:
                getActivity().onBackPressed();
                break;
        }
    }
}
