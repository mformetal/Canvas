package miles.scribble.ui.widget

import android.content.Context
import android.support.v7.widget.AppCompatEditText
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.inputmethod.InputMethodManager

/**
 * Created by mbpeele on 9/2/15.
 */
class OnBackPressedEditText : AppCompatEditText {

    private var mOnImeBack: BackPressedListener? = null

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    override fun onKeyPreIme(keyCode: Int, event: KeyEvent): Boolean {
        if (event.keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_UP) {
            mOnImeBack?.onImeBack(this)
        }
        return super.dispatchKeyEvent(event)
    }

    interface BackPressedListener {
        fun onImeBack(editText: OnBackPressedEditText)
    }
}
