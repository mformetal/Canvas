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



/**
 * Created by mbpeele on 6/30/17.
 */
class HomeStore @Inject constructor(context: Context) : SimpleStore<HomeState>(HomeState.create(context))

data class HomeState(val isMenuOpen : Boolean = false,
                     var width : Int = 0, var height : Int = 0,
                     val drawType: DrawType = HomeState.DrawType.DRAW,
                     var cachedBitmap: Bitmap,
                     var isSafeToDraw : Boolean = true) : State {

    companion object {
        private val STROKE_WIDTH = 5f
        private val INVALID_POINTER = -1
        private val NONE = 0
        private val DRAG = 1
        private val ZOOM = 2

        fun create(context: Context) : HomeState {
            val point = context.getDisplaySize()
            return HomeState(width = point.x, height = point.y, cachedBitmap = FileUtils.getCachedBitmap(context))
        }
    }

    enum class DrawType {
        DRAW,
        ERASE,
        TEXT,
        INK,
        PICTURE
    }

    private val matrix: Matrix = Matrix()
    private val savedMatrix: Matrix = Matrix()
    private val canvas: Canvas
    private val stroke: Stroke
    private val redoneHistory: Stack<Any> = Stack()
    private val history: Stack<Any> = Stack()
    private val paint: Paint
    private val state = DrawType.DRAW
    private val startPoint: PointF = PointF()
    private val midPoint: PointF = PointF()
    private val mActivePointer = INVALID_POINTER
    private val mLastX: Float = 0.toFloat()
    private val mLastY: Float = 0.toFloat()
    private val mStrokeColor: Int = 0
    private val mBackgroundColor: Int = 0
    private val mOppositeBackgroundColor: Int = 0
    private val mInkedColor: Int = 0
    private val mOldDist = 1.0
    private val mLastRotation = 0f
    private val shouldUpdate: Boolean = false
    var bitmap: Bitmap

    init {
        val strokeColor = ViewUtils.randomColor()
        paint = PaintStyles.normal(strokeColor, 5f)

        bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        bitmap.eraseColor(Color.WHITE)
        cachedBitmap.eraseColor(Color.WHITE)

        canvas = Canvas(bitmap)
        canvas.drawBitmap(cachedBitmap, 0f, 0f, null)

        stroke = Stroke(paint)
    }

}