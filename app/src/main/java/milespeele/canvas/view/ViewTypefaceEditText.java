package milespeele.canvas.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.support.design.widget.TextInputLayout;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewParent;
import android.widget.EditText;

import milespeele.canvas.util.Logg;
import milespeele.canvas.util.TextUtils;
import milespeele.canvas.util.ViewUtils;

/**
 * Created by mbpeele on 9/2/15.
 */
public class ViewTypefaceEditText extends EditText {

    private BackPressedListener mOnImeBack;

    public ViewTypefaceEditText(Context context) {
        super(context);
        init();
    }

    public ViewTypefaceEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ViewTypefaceEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public ViewTypefaceEditText(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
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

        setTypeface(TextUtils.getStaticTypeFace(getContext(), "Roboto.ttf"));
    }

    public String getTextAsString() { return getText().toString(); }

    public void setBackPressedListener(BackPressedListener listener) {
        mOnImeBack = listener;
    }

    public interface BackPressedListener {
        void onImeBack(ViewTypefaceEditText editText);
    }
}
