package miles.scribble.home.viewmodel

import android.graphics.Canvas
import miles.scribble.redux.core.StoreViewModel
import miles.scribble.redux.core.Store
import javax.inject.Inject
import io.realm.Realm
import android.graphics.Bitmap
import miles.scribble.data.Drawing
import miles.scribble.home.drawing.DrawType
import miles.scribble.util.extensions.DateExtensions
import miles.scribble.util.extensions.drawBitmap
import java.io.ByteArrayOutputStream


/**
 * Created by mbpeele on 6/28/17.
 */
class HomeViewModel @Inject constructor(homeStore: HomeStore) : StoreViewModel<HomeState, Store<HomeState>>(homeStore) {

    val realm : Realm = Realm.getDefaultInstance()

    override fun onCleared() {
        super.onCleared()

        realm.close()
    }

    fun persistDrawings() {
        realm.executeTransaction {
            val stream = ByteArrayOutputStream()
            state.bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            val bytes = stream.toByteArray()

            val hasCachedDrawing = it.where(Drawing::class.java).count() > 0
            if (hasCachedDrawing) {
                val drawing = it.where(Drawing::class.java).findFirst()
                drawing.lastEditedMillis = DateExtensions.currentTimeInMillis
                drawing.bytes = bytes
                it.insertOrUpdate(drawing)
            } else {
                val drawing = Drawing()
                drawing.createdAtMillis = DateExtensions.currentTimeInMillis
                drawing.lastEditedMillis = DateExtensions.currentTimeInMillis
                drawing.bytes = bytes
                it.insert(drawing)
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
                    val majorDimen = if (canvas.width > canvas.height) canvas.width else canvas.height
                    val minorDimen = if (canvas.width > canvas.height) canvas.height else canvas.width

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