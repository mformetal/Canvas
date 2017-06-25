package miles.scribble.ui.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

/**
 * Created by mbpeele on 9/2/15.
 */
public class OnBackPressedEditText extends EditText {

    private BackPressedListener mOnImeBack;

    public OnBackPressedEditText(Context context) {
        super(context);
        init();
    }

    public OnBackPressedEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public OnBackPressedEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public OnBackPressedEditText(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    @Override
    public boolean onKeyPreIme(int keyCode, KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
            if (mOnImeBack != null) {
                mOnImeBack.onImeBack(this);
            }
        }
        return super.dispatchKeyEvent(event);
    }

    private void init() {
        if (isInEditMode()) {
            return;
        }
    }

    public String getTextAsString() { return getText().toString(); }

    public void setBackPressedListener(BackPressedListener listener) {
        mOnImeBack = listener;
    }

    public void closeKeyboard() {
        InputMethodManager imm
                = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getWindowToken(), 0);
    }

    public interface BackPressedListener {
        void onImeBack(OnBackPressedEditText editText);
    }
}
