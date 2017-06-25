package miles.scribble.home.drawing

import android.graphics.Canvas
import android.graphics.Paint
import android.os.SystemClock

import java.util.ArrayList

/**
 * Created by mbpeele on 11/23/15.
 */
internal class Stroke(paint: Paint) : ArrayList<CanvasPoint>() {

    private val TOLERANCE = 5f

    val paint: Paint = Paint(paint)

    fun peek(): CanvasPoint {
        return get(size - 1)
    }

    fun addPoint(x: Float, y: Float, canvas: Canvas, paint: Paint) {
        val nextPoint: CanvasPoint
        if (isEmpty()) {
            nextPoint = CanvasPoint(x, y, SystemClock.currentThreadTimeMillis())

            paint.strokeWidth = paint.strokeWidth / 2
            canvas.drawPoint(x, y, paint)
            add(nextPoint)
            paint.strokeWidth = paint.strokeWidth * 2
        } else {
            val prevPoint = peek()

            if (Math.abs(prevPoint.x - x) < TOLERANCE && Math.abs(prevPoint.y - y) < TOLERANCE) {
                return
            }

            nextPoint = CanvasPoint(x, y, SystemClock.currentThreadTimeMillis())

            add(nextPoint)
            draw(prevPoint, nextPoint, canvas, paint)
        }
    }

    fun draw(previous: CanvasPoint, next: CanvasPoint, canvas: Canvas, paint: Paint) {
        canvas.drawLine(previous.x, previous.y, next.x, next.y, paint)
    }
}
