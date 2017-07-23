package miles.scribble.util.extensions

import android.animation.Animator
import android.animation.ObjectAnimator
import android.view.View
import android.support.v4.view.ViewCompat.setBackgroundTintList
import android.animation.AnimatorListenerAdapter
import android.animation.PropertyValuesHolder
import android.content.res.Resources
import android.os.Build
import miles.scribble.R


/**
 * Created by mbpeele on 7/23/17.
 */
fun View.scaleDown(scaleX: Float, scaleY: Float, duration : Long = 350) : ObjectAnimator {
    return ObjectAnimator.ofPropertyValuesHolder(this,
            PropertyValuesHolder.ofFloat(View.SCALE_X, scaleX),
            PropertyValuesHolder.ofFloat(View.SCALE_Y, scaleY))
            .setDuration(duration)
}

fun View.scaleUp(scaleX: Float, scaleY: Float, duration : Long = 350) : ObjectAnimator {
    return ObjectAnimator.ofPropertyValuesHolder(this,
            PropertyValuesHolder.ofFloat(View.SCALE_X, scaleX),
            PropertyValuesHolder.ofFloat(View.SCALE_Y, scaleY))
            .setDuration(duration)
}