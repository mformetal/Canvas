package miles.scribble.home.viewmodel

import android.arch.lifecycle.AndroidViewModel
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import miles.scribble.App
import miles.scribble.home.drawing.DrawType
import miles.scribble.util.extensions.drawBitmap
import miles.scribble.util.extensions.larger
import java.io.ByteArrayOutputStream
import java.util.concurrent.ExecutorService

/**
 * Created using mbpeele on 6/28/17.
 */
const val CACHED_DRAWING_NAME = "NAME"

class HomeViewModel(val store: HomeStore, app: App,
                    val threadExecutor: ExecutorService) : AndroidViewModel(app) {

    val state : HomeState
        get() = store.state

    fun cacheDrawing() {
        threadExecutor.submit {
            getApplication<App>()
                    .openFileOutput(CACHED_DRAWING_NAME, Context.MODE_PRIVATE)
                    .use {
                        val stream = ByteArrayOutputStream()
                        state.bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
                        val bytes = stream.toByteArray()
                        it.write(bytes)
                    }
        }
    }

    fun drawToSurfaceView(canvas: Canvas?) {
        if (canvas != null) {
            canvas.drawBitmap(state.bitmap)

            when (state.drawType) {
                DrawType.NORMAL -> {

                }
                DrawType.INK -> {
                    // To account for portrait/ landscape
                    val majorDimen = canvas.width larger canvas.height
                    val minorDimen = canvas.width larger canvas.height

                    val lineSize = majorDimen * .1f
                    val xSpace = majorDimen * .05f
                    val middleX = state.lastX
                    val middleY = state.lastY - minorDimen * .1f

                    // base "pointer"
                    state.paint.color = state.oppositeBackgroundColor
                    canvas.drawLine(middleX + xSpace / 2, middleY, middleX + xSpace + lineSize, middleY, state.paint)
                    canvas.drawLine(middleX - xSpace / 2, middleY, middleX - xSpace - lineSize, middleY, state.paint)
                    canvas.drawLine(middleX, middleY + xSpace / 2, middleX, middleY + xSpace + lineSize, state.paint)
                    canvas.drawLine(middleX, middleY - xSpace / 2, middleX, middleY - xSpace - lineSize, state.paint)

                    // ink circle
                    canvas.drawCircle(middleX, middleY, xSpace + lineSize, state.paint)

                    val oldWidth = state.paint.strokeWidth
                    state.paint.strokeWidth = 20f
                    state.paint.color = state.strokeColor
                    canvas.drawCircle(middleX, middleY, xSpace + lineSize - state.paint.strokeWidth, state.paint)
                    state.paint.strokeWidth = oldWidth
                }
                DrawType.ERASE -> {

                }
                DrawType.PICTURE -> {
                    val saveCount = canvas.save()
                    canvas.concat(state.photoState.matrix)
                    canvas.drawBitmap(state.photoState.photoBitmap!!, 0f, 0f, null)
                    canvas.restoreToCount(saveCount)
                }
            }
        }
    }
}