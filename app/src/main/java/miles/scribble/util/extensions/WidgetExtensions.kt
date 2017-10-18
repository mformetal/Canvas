package miles.scribble.util.extensions

import android.content.Context
import android.view.View
import android.view.ViewTreeObserver
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView
import java.lang.ref.SoftReference

/**
 * Created using mbpeele on 6/25/17.
 */
fun View.systemUIGone() {
    systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
            View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
            View.SYSTEM_UI_FLAG_FULLSCREEN or
            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
}

fun View.systemUIVisibile() {
    systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
            View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
}

fun View.radius() : Float {
    return measuredWidth / 2F
}

fun TextView.textString() : String {
    return text.toString()
}

fun EditText.closeKeyboard() {
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(windowToken, 0)
}

fun <T: View> T.addPreDrawListener(listener: (T) -> Unit) {
    val softReference = SoftReference(this)

    val view = this
    val viewTreeObserver = viewTreeObserver
    viewTreeObserver.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
        override fun onPreDraw(): Boolean {
            if (viewTreeObserver.isAlive) {
                viewTreeObserver.removeOnPreDrawListener(this)
            } else {
                view.viewTreeObserver.removeOnPreDrawListener(this)
            }

            val reference = softReference.get()
            return if (reference != null) {
                listener.invoke(reference)
                true
            } else {
                false
            }
        }
    })
}
