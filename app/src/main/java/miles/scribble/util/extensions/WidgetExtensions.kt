package miles.scribble.util.extensions

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.app.Activity
import android.content.Context
import android.support.annotation.IdRes
import android.support.v4.app.DialogFragment
import android.support.v4.app.Fragment
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView

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

fun View.relativeCenterX(): Float {
    return (left + right) / 2f
}

fun View.relativeCenterY(): Float {
    return (top + bottom) / 2f
}

fun View.goneAnimator(): ObjectAnimator {
    val gone = ObjectAnimator.ofFloat(this, View.ALPHA, 1f, 0f)
    gone.duration = 350L
    gone.addListener(object : AnimatorListenerAdapter() {
        override fun onAnimationEnd(animation: Animator) {
            this@goneAnimator.visibility = View.GONE
        }
    })
    return gone
}

fun View.visibleAnimator(): ObjectAnimator {
    val visibility = ObjectAnimator.ofFloat(this, View.ALPHA, 0f, 1f)
    visibility.duration = 350L
    visibility.addListener(object : AnimatorListenerAdapter() {
        override fun onAnimationStart(animation: Animator) {
            this@visibleAnimator.visibility = View.VISIBLE
        }
    })
    return visibility
}

fun <T : View> View.lazyInflate(@IdRes layoutId: Int) : Lazy<T> {
    return lazy { findViewById<T>(layoutId) }
}

fun <T : View> Fragment.lazyInflate(@IdRes layoutId: Int) : Lazy<T> {
    return lazy { view!!.findViewById<T>(layoutId) }
}

fun <T : View> DialogFragment.lazyInflate(view: View, @IdRes layoutId: Int) : Lazy<T> {
    return lazy { view.findViewById<T>(layoutId) }
}

fun <T : View> Activity.lazyInflate(@IdRes layoutId: Int) : Lazy<T> {
    return lazy { findViewById<T>(layoutId) }
}
