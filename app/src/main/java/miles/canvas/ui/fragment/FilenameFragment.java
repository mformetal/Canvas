package miles.canvas.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.inputmethod.EditorInfo;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import miles.canvas.R;
import miles.canvas.data.event.EventFilenameChosen;
import miles.canvas.ui.widget.TypefaceEditText;
import miles.canvas.util.ViewUtils;

/**
 * Created by Miles Peele on 7/13/2015.
 */
public class FilenameFragment extends BaseFragment implements View.OnClickListener, TypefaceEditText.BackPressedListener {

    @Bind(R.id.fragment_filename_input) TypefaceEditText input;

    public FilenameFragment() {}

    public static FilenameFragment newInstance() {
        return new FilenameFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_filename, container, false);
        ButterKnife.bind(this, v);
        input.setBackPressedListener(this);
        input.setOnClickListener(v1 -> {
            View view = (View) getView().getParent();

            int screenHeight = ViewUtils.displayHeight(getActivity());
            float keyboardPos = screenHeight * .5f;
            float viewBottom = view.getBottom();

            if (keyboardPos < viewBottom) {
                view.animate()
                        .setInterpolator(new AccelerateDecelerateInterpolator())
                        .translationYBy(-Math.abs(keyboardPos - viewBottom));
            }
        });
        input.setOnEditorActionListener((v1, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                View view = (View) getView().getParent();
                if (view.getTranslationY() != 0) {
                    view.animate()
                            .setInterpolator(new AccelerateDecelerateInterpolator())
                            .translationY(0);
                }
            }
            return false;
        });
        return v;
    }

    @Override
    @OnClick({R.id.fragment_filename_pos_button, R.id.fragment_filename_neg_button})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fragment_filename_pos_button:
                if (validateFileName()) {
                    bus.post(new EventFilenameChosen(input.getTextAsString()));
                    input.closeKeyboard();
                    getActivity().onBackPressed();
                }
                break;
            case R.id.fragment_filename_neg_button:
                getActivity().onBackPressed();
                break;
        }
    }


    @Override
    public void onImeBack(TypefaceEditText editText) {
        View view = (View) getView().getParent();
        if (view.getTranslationY() != 0) {
            view.animate()
                    .setInterpolator(new AccelerateDecelerateInterpolator())
                    .translationY(0);
        }
    }

    private boolean validateFileName() {
        String name = input.getTextAsString();
        if (name.length() == 0) {
            input.setError("Filename must not be empty");
            return false;
        }

        return true;
    }
}
