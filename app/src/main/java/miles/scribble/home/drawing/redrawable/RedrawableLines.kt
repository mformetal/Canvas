package miles.scribble.home.drawing.redrawable

import android.graphics.Canvas
import android.graphics.Paint
import miles.scribble.home.drawing.CanvasPoint

/**
 * Created by mbpeele on 1/4/16.
 */
internal class RedrawableLines(points: List<CanvasPoint>, paint: Paint) : Redrawable {

    var lines: FloatArray = FloatArray(0)
    val paint: Paint = Paint(paint)

    init {
        lines = storePoints(points)
    }

    override fun draw(canvas: Canvas) {
        if (lines.isNotEmpty()) {
            canvas.drawLines(lines, paint)
        }
    }

    private fun storePoints(points: List<CanvasPoint>): FloatArray {
        val length = points.size

        val n = length * 2
        val arraySize = n + (n - 4)

        if (arraySize <= 0) {
            return FloatArray(0)
        }

        val pts = FloatArray(arraySize)
        var counter = 1

        for (ndx in 0..length - 1) {
            val x = points[ndx].x
            val y = points[ndx].y

            if (ndx == 0) {
                pts[ndx] = x
                pts[ndx + 1] = y
                continue
            }

            if (ndx == length - 1) {
                pts[pts.size - 2] = points[ndx].x
                pts[pts.size - 1] = points[ndx].y
                break
            }

            val newNdx = ndx + counter
            counter += 3

            pts[newNdx] = x
            pts[newNdx + 1] = y
            pts[newNdx + 2] = x
            pts[newNdx + 3] = y
        }
        return pts
    }
}
