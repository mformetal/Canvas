package miles.scribble.util

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Rect
import android.util.Property
import android.view.View

import java.util.Random

/**
 * Created by mbpeele on 11/4/15.
 */
object ViewUtils {

    val BACKGROUND = "backgroundColor"
    private val DEFAULT_VISBILITY_DURATION = 350

    abstract class FloatProperty<T>(name: String) : Property<T, Float>(Float::class.java, name) {

        abstract fun setValue(receiver: T, value: Float)

        override fun set(receiver: T, value: Float) {
            setValue(receiver, value)
        }
    }

    abstract class IntProperty<T>(name: String) : Property<T, Int>(Int::class.java, name) {

        abstract fun setValue(receiver: T, value: Int)

        override fun set(receiver: T, value: Int) {
            setValue(receiver, value)
        }
    }

    fun randomColor(): Int {
        val random = Random()
        return Color.argb(255, random.nextInt(256), random.nextInt(256), random.nextInt(256))
    }

    fun systemUIGone(decorView: View) {
        decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
    }

    fun relativeCenterX(view: View): Float {
        return (view.left + view.right) / 2f
    }

    fun relativeCenterY(view: View): Float {
        return (view.top + view.bottom) / 2f
    }

    fun complementColor(color: Int): Int {
        val alpha = Color.alpha(color)
        var red = Color.red(color)
        var blue = Color.blue(color)
        var green = Color.green(color)

        red = red.inv() and 0xff
        blue = blue.inv() and 0xff
        green = green.inv() and 0xff

        return Color.argb(alpha, red, green, blue)
    }

    fun radius(view: View): Float {
        return view.measuredWidth / 2f
    }

    fun gone(view: View, duration: Int) {
        goneAnimator(view).setDuration(duration.toLong()).start()
    }

    fun goneAnimator(view: View): ObjectAnimator {
        val gone = ObjectAnimator.ofFloat(view, View.ALPHA, 1f, 0f)
        gone.duration = DEFAULT_VISBILITY_DURATION.toLong()
        gone.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                view.visibility = View.GONE
            }
        })
        return gone
    }

    fun visible(view: View, duration: Int) {
        if (view.visibility != View.VISIBLE) {
            visibleAnimator(view).setDuration(duration.toLong()).start()
        }
    }

    fun visible(view: View) {
        if (view.visibility != View.VISIBLE) {
            visibleAnimator(view).start()
        }
    }

    fun visibleAnimator(view: View): ObjectAnimator {
        val visibility = ObjectAnimator.ofFloat(view, View.ALPHA, 0f, 1f)
        visibility.duration = DEFAULT_VISBILITY_DURATION.toLong()
        visibility.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationStart(animation: Animator) {
                view.visibility = View.VISIBLE
            }
        })
        return visibility
    }

}
