package miles.scribble.home.drawing

import android.graphics.Canvas
import android.graphics.Paint
import android.os.SystemClock

import java.util.ArrayList

/**
 * Created by mbpeele on 11/23/15.
 */
data class Stroke(val points : ArrayList<CanvasPoint> = ArrayList()) {

    private val TOLERANCE = 5f

    fun addPoint(x: Float, y: Float, canvas: Canvas, paint: Paint) {
        val nextPoint: CanvasPoint = if (points.isEmpty()) {
             CanvasPoint(x, y).apply {
                 paint.strokeWidth = paint.strokeWidth / 2
                 canvas.drawPoint(x, y, paint)
                 paint.strokeWidth = paint.strokeWidth * 2
             }
        } else {
            val prevPoint = points.last()

            if (Math.abs(prevPoint.x - x) < TOLERANCE && Math.abs(prevPoint.y - y) < TOLERANCE) {
                return
            }

            CanvasPoint(x, y).apply {
                canvas.drawLine(prevPoint.x, prevPoint.y, x, y, paint)
            }
        }

        points.add(nextPoint)
    }
}
