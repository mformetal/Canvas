package miles.scribble.home.viewmodel

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import miles.scribble.redux.core.StoreViewModel
import miles.scribble.redux.core.Store
import miles.scribble.util.extensions.getDisplaySize
import javax.inject.Inject

/**
 * Created by mbpeele on 6/28/17.
 */
class HomeViewModel @Inject constructor(homeStore: HomeStore) : StoreViewModel<HomeState, Store<HomeState>>(homeStore) {

    fun drawToSurfaceView(canvas: Canvas?) {
        canvas?.drawBitmap(state.bitmap, 0f, 0f, null)
    }
}