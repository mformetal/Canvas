package miles.scribble.util

import android.graphics.Color
import android.util.Property
import java.util.*

/**
 * Created from mbpeele on 11/4/15.
 */
object ViewUtils {

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
}
