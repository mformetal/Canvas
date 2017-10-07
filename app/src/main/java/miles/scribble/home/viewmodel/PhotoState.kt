package miles.scribble.home.viewmodel

import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.PointF
import miles.redux.core.State

/**
 * Created by mbpeele on 8/13/17.
 */
data class PhotoState(val matrix: Matrix = Matrix(),
                      var photoBitmap : Bitmap?= null,
                      val savedMatrix: Matrix = Matrix(),
                      val startPoint: PointF = PointF(),
                      val midPoint: PointF = PointF(),
                      val photoMode: PhotoMode = PhotoMode.NONE,
                      val oldDistance: Float = 1f,
                      val lastRotation: Float = 0f) : State

enum class PhotoMode {
    NONE,
    DRAG,
    ZOOM
}