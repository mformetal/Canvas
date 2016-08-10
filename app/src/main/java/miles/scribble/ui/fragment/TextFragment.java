package miles.scribble.ui.fragment;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;

import miles.scribble.R;
import miles.scribble.data.event.EventTextChosen;
import miles.scribble.ui.widget.TypefaceEditText;
import miles.scribble.util.ViewUtils;

/**
 * Created by mbpeele on 11/14/15.
 */
public class TextFragment extends BaseFragment implements View.OnClickListener, TypefaceEditText.BackPressedListener {

    TypefaceEditText input;

    public TextFragment() {}

    public static TextFragment newInstance() {
        return new TextFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_text, container, false);
        input.setBackPressedListener(this);
        input.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View view = (View) getView().getParent();

                int screenHeight = ViewUtils.displayHeight(getActivity());
                float keyboardPos = screenHeight * .5f;
                float viewBottom = view.getBottom();

                if (keyboardPos < viewBottom) {
                    view.animate()
                            .setInterpolator(new AccelerateDecelerateInterpolator())
                            .translationYBy(-Math.abs(keyboardPos - viewBottom));
                }
            }
        });

        input.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    View view = (View) getView().getParent();
                    if (view.getTranslationY() != 0) {
                        view.animate()
                                .setInterpolator(new AccelerateDecelerateInterpolator())
                                .translationY(0);
                    }
                }
                return false;
            }
        });

        return v;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fragment_text_pos_button:
                if (validateEnteredText()) {
                    bus.post(new EventTextChosen(input.getTextAsString()));
                    input.closeKeyboard();
                    getActivity().onBackPressed();
                }
                break;
            case R.id.fragment_text_neg_button:
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

    private boolean validateEnteredText() {
        String text = input.getTextAsString();
        if (text.length() == 0) {
            input.setError("Text must not be empty!");
            return false;
        }

        return true;
    }
}
