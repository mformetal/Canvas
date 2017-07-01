package miles.scribble.home.viewmodel

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.view.MotionEvent
import miles.scribble.R.id.width
import miles.scribble.home.drawing.DrawingCurve
import miles.scribble.redux.core.SimpleStore
import miles.scribble.redux.core.StoreViewModel
import miles.scribble.redux.core.Store
import miles.scribble.util.FileUtils
import miles.scribble.util.extensions.getDisplaySize
import javax.inject.Inject

/**
 * Created by mbpeele on 6/28/17.
 */
class HomeViewModel @Inject constructor(homeStore: HomeStore) : StoreViewModel<HomeState, Store<HomeState>>(homeStore) {

    val drawingCurve : DrawingCurve = DrawingCurve(this)

    fun drawToSurfaceView(canvas: Canvas?) {
        drawingCurve.drawToSurfaceView(canvas)
    }

    fun onTouchEvent(event: MotionEvent) : Boolean {
//        when (event.action and MotionEvent.ACTION_MASK) {
//            MotionEvent.ACTION_DOWN -> onTouchDown(event)
//
//            MotionEvent.ACTION_POINTER_DOWN -> onPointerDown(event)
//
//            MotionEvent.ACTION_MOVE -> onTouchMove(event)
//
//            MotionEvent.ACTION_UP -> onTouchUp(event)
//
//            MotionEvent.ACTION_POINTER_UP -> onPointerUp(event)
//
//            MotionEvent.ACTION_CANCEL -> onCancel(event)
//        }

        return true
    }
}