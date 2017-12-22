package miles.scribble.home.viewmodel

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.support.v4.widget.ViewDragHelper.INVALID_POINTER
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import miles.dispatch.core.SimpleStore
import miles.dispatch.core.Event
import miles.dispatch.core.State
import miles.scribble.home.drawing.DrawType
import miles.scribble.home.drawing.Stroke
import miles.scribble.home.drawing.redrawable.DrawHistory
import miles.scribble.util.PaintStyles
import miles.scribble.util.ViewUtils

/**
 * Created using mbpeele on 6/30/17.
 */
class HomeStore(bitmap: Bitmap) : SimpleStore<HomeState>(HomeState.create(bitmap))

data class HomeState(val isMenuOpen : Boolean = false,
                     val isSafeToDraw : Boolean = true,
                     val drawType: DrawType = DrawType.NORMAL,
                     val paint : Paint = PaintStyles.normal(ViewUtils.randomColor(), STROKE_WIDTH),
                     val backgroundColor : Int = Color.WHITE,
                     val strokeColor : Int = paint.color,
                     val stroke : Stroke = Stroke(),
                     val activePointer : Int = INVALID_POINTER,
                     val lastX : Float = 0f,
                     val lastY : Float = 0f,
                     val history : DrawHistory = DrawHistory(),
                     val bitmap: Bitmap,
                     val canvas: Canvas,
                     val photoState: PhotoState = PhotoState(),
                     val onClickSubject: Subject<Event> = PublishSubject.create<Event>()) : State {

    val oppositeBackgroundColor : Int
        get() = ViewUtils.complementColor(backgroundColor)

    val width : Int
        get() = bitmap.width
    val height : Int
        get() = bitmap.height

    companion object {
        val STROKE_WIDTH = 5f

        fun create(bitmap: Bitmap) : HomeState {
            return HomeState(bitmap = bitmap, canvas = Canvas(bitmap))
        }
    }
}