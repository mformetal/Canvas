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

/**
 * Created by mbpeele on 6/28/17.
 */
class HomeViewModel @Inject constructor(homeStore: HomeStore) : StoreViewModel<HomeState, Store<HomeState>>(homeStore) {

    fun drawToSurfaceView(canvas: Canvas?) {
        if (state.isSafeToDraw) {
            canvas?.drawBitmap(state.bitmap, 0f, 0f, null)
        }
    }

    fun redraw(isUndo: Boolean, dispatcher: Dispatcher<CircleMenuEvents, CircleMenuEvents>) {
        dispatcher.dispatch(CircleMenuEvents.RedrawStarted(isUndo))

        for (any in state.history) {
            dispatcher.dispatch(CircleMenuEvents.Redraw(any))
        }

        dispatcher.dispatch(CircleMenuEvents.RedrawCompleted())
    }
}