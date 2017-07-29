package miles.scribble.home.viewmodel

import android.graphics.Canvas
import io.reactivex.Flowable
import io.reactivex.FlowableSubscriber
import io.reactivex.schedulers.Schedulers
import miles.scribble.home.events.CircleMenuEvents
import miles.scribble.redux.core.Dispatcher
import miles.scribble.redux.core.StoreViewModel
import miles.scribble.redux.core.Store
import org.reactivestreams.Subscription
import javax.inject.Inject
import android.opengl.ETC1.getHeight
import android.opengl.ETC1.getWidth
import io.realm.Realm
import android.graphics.Bitmap
import android.R.attr.bitmap
import miles.scribble.data.Drawing
import miles.scribble.util.extensions.DateExtensions
import miles.scribble.util.extensions.drawBitmap
import org.threeten.bp.Instant
import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZoneOffset
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

            if (state.drawType == HomeState.DrawType.INK) {
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
        }
    }
}