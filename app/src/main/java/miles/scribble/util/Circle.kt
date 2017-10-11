package miles.scribble.util

import android.graphics.RectF

/**
 * Created using mbpeele on 12/17/15.
 */
class Circle(centerX: Float, centerY: Float, val radius: Float) {

    private val boundingRect: RectF = RectF()

    var cx : Float = centerX
        get() = boundingRect.centerX()
        set(value) {
            field = value
            boundingRect.left = value - radius
            boundingRect.right = value + radius
        }

    var cy : Float = centerY
        get() = boundingRect.centerY()
        set(value) {
            field = value
            boundingRect.top = value - radius
            boundingRect.bottom = value + radius
        }

    init {
        boundingRect.set(centerX - radius,
                centerY - radius,
                centerX + radius,
                centerY + radius)

        cx = boundingRect.centerX()
        cy = boundingRect.centerY()
    }

    fun contains(x: Float, y: Float): Boolean {
        return Math.pow((cx - x).toDouble(), 2.0) + Math.pow((cy - y).toDouble(), 2.0) <= radius * radius
    }

    fun angleInDegrees(x: Float, y: Float): Double {
        return Math.toDegrees(Math.atan2((cy - y).toDouble(), (cx - x).toDouble()))
    }
}
