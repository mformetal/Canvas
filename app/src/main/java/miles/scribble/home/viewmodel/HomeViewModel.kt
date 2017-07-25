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



/**
 * Created by mbpeele on 6/28/17.
 */
class HomeViewModel @Inject constructor(homeStore: HomeStore) : StoreViewModel<HomeState, Store<HomeState>>(homeStore) {

    fun drawToSurfaceView(canvas: Canvas?) {
        if (state.isSafeToDraw && canvas != null) {
            canvas.drawBitmap(state.bitmap, 0f, 0f, null)

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