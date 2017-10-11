package miles.scribble.util

import android.graphics.BlurMaskFilter
import android.graphics.ComposePathEffect
import android.graphics.CornerPathEffect
import android.graphics.DashPathEffect
import android.graphics.EmbossMaskFilter
import android.graphics.Paint

/**
 * Created using Miles Peele on 7/26/2015.
 */
object PaintStyles {

    fun normal(currentColor: Int, width: Float): Paint {
        val paint = Paint()
        paint.isAntiAlias = true
        paint.isDither = true
        paint.color = currentColor
        paint.style = Paint.Style.STROKE
        paint.strokeJoin = Paint.Join.ROUND
        paint.strokeWidth = width
        paint.strokeCap = Paint.Cap.ROUND
        return paint
    }

    fun emboss(currentColor: Int, width: Float): Paint {
        val paint = Paint()
        paint.isAntiAlias = true
        paint.isDither = true
        paint.color = currentColor
        paint.strokeWidth = width
        paint.style = Paint.Style.STROKE
        paint.strokeJoin = Paint.Join.ROUND
        paint.strokeCap = Paint.Cap.ROUND
        paint.maskFilter = EmbossMaskFilter(floatArrayOf(1f, 1f, 1f), 0.4f, 10f, 8.2f)
        return paint
    }

    fun deboss(currentColor: Int, width: Float): Paint {
        val paint = Paint()
        paint.isAntiAlias = true
        paint.isDither = true
        paint.color = currentColor
        paint.strokeWidth = width
        paint.style = Paint.Style.STROKE
        paint.strokeJoin = Paint.Join.ROUND
        paint.strokeCap = Paint.Cap.ROUND
        paint.maskFilter = EmbossMaskFilter(floatArrayOf(0f, -1f, 0.5f), 0.8f, 13f, 7f)
        return paint
    }

    fun dots(currentColor: Int, width: Float): Paint {
        val paint = Paint()
        paint.isAntiAlias = true
        paint.isDither = true
        paint.color = currentColor
        paint.strokeWidth = width
        paint.style = Paint.Style.STROKE
        paint.strokeJoin = Paint.Join.ROUND
        paint.strokeCap = Paint.Cap.ROUND
        paint.pathEffect = ComposePathEffect(
                DashPathEffect(floatArrayOf(1f, 51f), 0f),
                CornerPathEffect(1f))
        return paint
    }

    fun normalShadow(color: Int, width: Float): Paint {
        val paint = Paint()
        paint.isAntiAlias = true
        paint.isDither = true
        paint.color = color
        paint.strokeWidth = width
        paint.style = Paint.Style.STROKE
        paint.maskFilter = BlurMaskFilter(15f, BlurMaskFilter.Blur.NORMAL)
        return paint
    }

    fun innerShadow(color: Int, width: Float): Paint {
        val paint = Paint()
        paint.isAntiAlias = true
        paint.isDither = true
        paint.color = color
        paint.strokeWidth = width
        paint.style = Paint.Style.STROKE
        paint.maskFilter = BlurMaskFilter(15f, BlurMaskFilter.Blur.INNER)
        return paint
    }

    fun outerShadow(color: Int, width: Float): Paint {
        val paint = Paint()
        paint.isAntiAlias = true
        paint.isDither = true
        paint.color = color
        paint.strokeWidth = width
        paint.style = Paint.Style.STROKE
        paint.maskFilter = BlurMaskFilter(15f, BlurMaskFilter.Blur.OUTER)
        return paint
    }

    fun solidShadow(color: Int, width: Float): Paint {
        val paint = Paint()
        paint.isAntiAlias = true
        paint.isDither = true
        paint.color = color
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = width
        paint.maskFilter = BlurMaskFilter(15f, BlurMaskFilter.Blur.SOLID)
        return paint
    }
}