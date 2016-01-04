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
import milespeele.canvas.event.EventTextChosen;
import milespeele.canvas.view.ViewTypefaceEditText;

/**
 * Created by mbpeele on 11/14/15.
 */
public class FragmentText extends FragmentBase implements View.OnClickListener {

    @Bind(R.id.fragment_text_input) ViewTypefaceEditText input;

    public FragmentText() {}

    public static FragmentText newInstance() {
        return new FragmentText();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_text, container, false);
        ButterKnife.bind(this, v);
        return v;
    }

    @Override
    @OnClick({R.id.fragment_text_pos_button, R.id.fragment_text_neg_button, R.id.fragment_text_input})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fragment_text_pos_button:
                String text = input.getTextAsString();
                if (text.isEmpty()) {
                    Toast.makeText(getActivity(),
                            R.string.toast_fragment_filename_invalid,
                            Toast.LENGTH_SHORT).show();
                } else {
                    bus.post(new EventTextChosen(input.getTextAsString()));
                    getActivity().onBackPressed();
                }
                break;
            case R.id.fragment_text_neg_button:
                getActivity().onBackPressed();
                break;
        }
    }
}
