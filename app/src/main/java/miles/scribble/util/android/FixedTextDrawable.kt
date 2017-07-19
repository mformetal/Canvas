package miles.scribble.util.android

import android.graphics.*
import android.graphics.drawable.Drawable
import android.text.TextPaint
import android.widget.TextView

/**
 * Created by mbpeele on 7/18/17.
 */
class FixedTextDrawable(private val fixedText: String, textView: TextView) : Drawable() {

    private val paint : TextPaint = textView.paint
    private val rect = Rect()
    private val textHeight : Int

    init {
        paint.getTextBounds(fixedText, 0, fixedText.length, rect)
        textHeight = rect.height()
    }

    override fun draw(canvas: Canvas) {
        canvas.drawText(fixedText,
                0F,
                (rect.centerY() + textHeight).toFloat(),
                paint)
    }

    override fun setAlpha(alpha: Int) {
        paint.alpha = alpha
    }

    override fun getOpacity(): Int {
        return PixelFormat.TRANSLUCENT
    }

    override fun setColorFilter(colorFilter: ColorFilter) {
        paint.colorFilter = colorFilter
    }
}