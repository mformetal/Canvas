package miles.scribble.util

import android.graphics.RectF

/**
 * Created by mbpeele on 12/17/15.
 */
class Circle(centerX: Float, centerY: Float, val radius: Float) {

    private val boundingRect: RectF = RectF()

    val diameter : Float
        get() = radius * 2

    var cx : Float
        get() = boundingRect.centerX()
        private set

    var cy : Float
        get() = boundingRect.centerY()
        private set

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

    fun setCenterX(x: Float) {
        boundingRect.left = x - radius
        boundingRect.right = x + radius
    }

    fun setCenterY(y: Float) {
        boundingRect.top = y - radius
        boundingRect.bottom = y + radius
    }
}
