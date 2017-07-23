package miles.scribble.home.viewmodel

import android.content.Context
import android.graphics.*
import miles.scribble.home.drawing.Stroke
import miles.scribble.redux.core.SimpleStore
import miles.scribble.redux.core.State
import miles.scribble.util.FileUtils
import miles.scribble.util.PaintStyles
import miles.scribble.util.ViewUtils
import miles.scribble.util.extensions.getDisplaySize
import java.util.*
import javax.inject.Inject
import android.support.v4.widget.ViewDragHelper.INVALID_POINTER
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import miles.scribble.home.drawing.redrawable.Redrawable


/**
 * Created by mbpeele on 6/30/17.
 */
class HomeStore @Inject constructor(context: Context) : SimpleStore<HomeState>(HomeState.create(context))

data class HomeState(val isMenuOpen : Boolean = false,
                     val width : Int, val height : Int,
                     val drawType: DrawType = HomeState.DrawType.DRAW,
                     val cachedBitmap: Bitmap,
                     val isSafeToDraw : Boolean = true,
                     val paint : Paint = PaintStyles.normal(ViewUtils.randomColor(), STROKE_WIDTH),
                     val backgroundColor : Int = Color.WHITE,
                     val strokeColor : Int = paint.color,
                     val stroke : Stroke = Stroke(),
                     val activePointer : Int = INVALID_POINTER,
                     val lastX : Float = 0f,
                     val lastY : Float = 0f,
                     val history: Stack<Redrawable> = Stack(),
                     val redoHistory : Stack<Redrawable> = Stack(),
                     val bitmap: Bitmap,
                     val canvas: Canvas,
                     val onClickSubject: Subject<Int> = PublishSubject.create<Int>()) : State {

    companion object {
        val INVALID_POINTER = -1
        val STROKE_WIDTH = 5f

        fun create(context: Context) : HomeState {
            val point = context.getDisplaySize()
            val bitmap = Bitmap.createBitmap(point.x, point.y, Bitmap.Config.ARGB_8888)
            return HomeState(width = point.x, height = point.y, cachedBitmap = FileUtils.getCachedBitmap(context),
                    bitmap = bitmap, canvas = Canvas(bitmap)).apply {
                canvas.drawBitmap(cachedBitmap, 0f, 0f, null)
            }
        }
    }

    enum class DrawType {
        DRAW,
        ERASE
    }
}