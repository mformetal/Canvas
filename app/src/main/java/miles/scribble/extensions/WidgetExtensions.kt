package miles.scribble.extensions

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.support.design.widget.FloatingActionButton
import android.view.View

/**
 * Created by mbpeele on 6/25/17.
 */
fun FloatingActionButton.scaleUp() {
    val scaleUp = ObjectAnimator.ofPropertyValuesHolder(this,
            PropertyValuesHolder.ofFloat(View.SCALE_X, 1f, 1.1f),
            PropertyValuesHolder.ofFloat(View.SCALE_Y, 1f, 1.1f))
            .setDuration(350)
    scaleUp.start()
}

fun FloatingActionButton.scaleDown() {
    val scaleDown = ObjectAnimator.ofPropertyValuesHolder(this,
            PropertyValuesHolder.ofFloat(View.SCALE_X, 1f),
            PropertyValuesHolder.ofFloat(View.SCALE_Y, 1f))
            .setDuration(350)
    scaleDown.start()
}

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
