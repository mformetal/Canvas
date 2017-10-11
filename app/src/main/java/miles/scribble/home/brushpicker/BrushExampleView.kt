package miles.scribble.home.brushpicker

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View
import android.R.attr.path



/**
 * Created using mbpeele on 7/29/17.
 */
class BrushExampleView : View {

    var paint : Paint ?= null
        set(value) {
            field = value
            invalidate()
        }
    private val path : Path = Path()

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val width = measuredWidth
        setMeasuredDimension(width, width / 4)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        path.rewind()

        val startX = w / 10f
        val endX = w - startX
        path.moveTo(startX, h / 2f)
        path.cubicTo(endX / 8, h / 4f,
                endX * .375f, h / 4f,
                endX / 2, h / 2f)
        path.cubicTo(endX * .675f, h * .75f,
                endX * .875f, h * .75f,
                endX, h / 2f)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        paint?.let {
            canvas.drawPath(path, paint)
        }
    }
}