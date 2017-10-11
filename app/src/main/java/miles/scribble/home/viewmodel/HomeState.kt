package miles.scribble.home.viewmodel

import android.content.Context
import android.graphics.*
import android.support.v4.widget.ViewDragHelper.INVALID_POINTER
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import miles.redux.core.Event
import miles.redux.core.SimpleStore
import miles.redux.core.State
import miles.scribble.home.drawing.DrawType
import miles.scribble.home.drawing.Stroke
import miles.scribble.home.drawing.redrawable.DrawHistory
import miles.scribble.util.PaintStyles
import miles.scribble.util.ViewUtils
import miles.scribble.util.extensions.getDisplaySize


/**
 * Created using mbpeele on 6/30/17.
 */
class HomeStore(context: Context) : SimpleStore<HomeState>(HomeState.create(context.getDisplaySize()))

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

        fun create(displaySize : Point) : HomeState {
            val bitmap = Bitmap.createBitmap(displaySize.x, displaySize.y, Bitmap.Config.ARGB_8888).apply {
                eraseColor(Color.WHITE)
            }
            return HomeState(bitmap = bitmap, canvas = Canvas(bitmap))
        }
    }
}