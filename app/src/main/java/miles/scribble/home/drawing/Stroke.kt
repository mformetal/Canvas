package miles.scribble.home.drawing

import android.graphics.Canvas
import android.graphics.Paint
import android.os.SystemClock

import java.util.ArrayList

/**
 * Created by mbpeele on 11/23/15.
 */
data class Stroke(val points : List<CanvasPoint> = listOf()) {

    private val TOLERANCE = 5f

    fun peek(): CanvasPoint {
        return points.last().copy()
    }

    fun addPoint(x: Float, y: Float, canvas: Canvas, paint: Paint) : Stroke {
        val nextPoint: CanvasPoint = if (points.isEmpty()) {
             CanvasPoint(x, y).apply {
                 canvas.drawPoint(x, y, paint)
             }
        } else {
            val prevPoint = peek()

            if (Math.abs(prevPoint.x - x) < TOLERANCE && Math.abs(prevPoint.y - y) < TOLERANCE) {
                return this
            }

            CanvasPoint(x, y).apply {
                canvas.drawLine(prevPoint.x, prevPoint.y, x, y, paint)
            }
        }

        return Stroke(points = ArrayList(points).apply { add(nextPoint) })
    }
}
