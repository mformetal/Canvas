package miles.scribble.home.drawing.drawhistory

import android.graphics.Canvas
import android.graphics.Matrix
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint

/**
 * Created by mbpeele on 1/4/16.
 */
internal class TextDrawHistory(var text: CharSequence, var matrixValues: FloatArray, paint: TextPaint) {

    val paint: TextPaint = TextPaint(paint)

    fun draw(canvas: Canvas, matrix: Matrix) {
        val layout = StaticLayout(text, paint, canvas.width,
                Layout.Alignment.ALIGN_CENTER, 1f, 1f, false)

        val prevMatrixValues = FloatArray(9)
        matrix.getValues(prevMatrixValues)

        matrix.setValues(matrixValues)

        canvas.save()
        canvas.concat(matrix)
        layout.draw(canvas)
        canvas.restore()

        matrix.setValues(prevMatrixValues)
    }
}
