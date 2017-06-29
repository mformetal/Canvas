package miles.scribble.home.drawing.drawhistory

import android.content.Context
import android.graphics.Canvas
import android.graphics.Matrix
import android.net.Uri
import miles.scribble.util.BitmapCache

import java.io.IOException
import java.io.InputStream

/**
 * Created by mbpeele on 1/4/16.
 */
internal class BitmapDrawHistory(var uri: Uri, var matrixValues: FloatArray) {

    fun draw(matrix: Matrix, cache: BitmapCache, context: Context, canvas: Canvas) {
        val prevMatrixValues = FloatArray(9)
        matrix.getValues(prevMatrixValues)

        var bitmap = cache.get(uri)
        if (bitmap == null) {
            val inputStream: InputStream?
            try {
                inputStream = context.contentResolver.openInputStream(uri)
                bitmap = cache.decodeStream(inputStream)

                inputStream?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }

        }

        matrix.setValues(matrixValues)

        canvas.save()
        canvas.concat(matrix)
        canvas.drawBitmap(bitmap, 0f, 0f, null)
        canvas.restore()

        matrix.setValues(prevMatrixValues)
    }
}
