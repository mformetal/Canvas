package miles.scribble.home.viewmodel

import android.graphics.Canvas
import miles.redux.core.Store
import miles.scribble.home.drawing.DrawType
import miles.scribble.util.android.StoreViewModel
import miles.scribble.util.extensions.drawBitmap
import miles.scribble.util.extensions.larger

/**
 * Created using mbpeele on 6/28/17.
 */
class HomeViewModel(homeStore: HomeStore) : StoreViewModel<HomeState, Store<HomeState>>(homeStore) {

    fun persistDrawings() {
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