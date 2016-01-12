package milespeele.canvas.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.inputmethod.EditorInfo;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import milespeele.canvas.R;
import milespeele.canvas.event.EventTextChosen;
import milespeele.canvas.util.ViewUtils;
import milespeele.canvas.view.ViewTypefaceEditText;

/**
 * Created by mbpeele on 11/14/15.
 */
public class FragmentText extends FragmentBase implements View.OnClickListener, ViewTypefaceEditText.BackPressedListener {

    @Bind(R.id.fragment_text_input) ViewTypefaceEditText input;

    public FragmentText() {}

    public static FragmentText newInstance() {
        return new FragmentText();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_text, container, false);
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
    @OnClick({R.id.fragment_text_pos_button, R.id.fragment_text_neg_button, R.id.fragment_text_input})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fragment_text_pos_button:
                if (validateEnteredText()) {
                    bus.post(new EventTextChosen(input.getTextAsString()));
                    getActivity().onBackPressed();
                }
                break;
            case R.id.fragment_text_neg_button:
                getActivity().onBackPressed();
                break;
        }
    }

    @Override
    public void onImeBack(ViewTypefaceEditText editText) {
        View view = (View) getView().getParent();
        if (view.getTranslationY() != 0) {
            view.animate()
                    .setInterpolator(new AccelerateDecelerateInterpolator())
                    .translationY(0);
        }
    }

    private boolean validateEnteredText() {
        String text = input.getTextAsString();
        if (text.length() == 0) {
            input.setError("Text must not be empty!");
            return false;
        }

        return true;
    }
}
