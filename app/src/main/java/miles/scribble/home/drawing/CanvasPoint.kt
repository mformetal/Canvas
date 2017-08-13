package miles.scribble.home.drawing

import android.os.SystemClock
import miles.scribble.util.extensions.DateExtensions

/**
 * Created by mbpeele on 11/29/15.
 */
data class CanvasPoint(var x: Float, var y: Float, var time: Long = DateExtensions.currentTimeInMillis) {

    fun computeDistance(p: CanvasPoint): Float {
        val dx = x - p.x
        val dy = y - p.y
        return Math.sqrt((dx * dx + dy * dy).toDouble()).toFloat()
    }

    fun computeVelocity(p: CanvasPoint): Float {
        val duration = Math.abs(time - p.time)
        return if (duration > 0L) computeDistance(p) / duration else computeDistance(p)
    }

    fun computeMidpoint(p2: CanvasPoint): CanvasPoint {
        return CanvasPoint((x + p2.x) / 2.0f, (y + p2.y) / 2.0f, (time + p2.time) / 2)
    }
}
