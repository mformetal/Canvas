package miles.scribble.home.drawing

import android.graphics.Canvas
import android.graphics.Paint
import assertk.assert
import assertk.assertions.isEqualTo
import com.nhaarman.mockito_kotlin.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Created using mbpeele on 8/12/17.
 */
@RunWith(RobolectricTestRunner::class)
class StrokeTest {

    lateinit var stroke : Stroke
    val canvas = mock<Canvas>()
    val paint = mock<Paint>()

    @Test
    fun testAddingInitialPoint() {
        stroke = Stroke()
        val point = CanvasPoint(0f, 0f)
        stroke.addPoint(point.x, point.y, canvas, paint)

        assert(stroke.points.size).isEqualTo(1)

        verify(canvas).drawPoint(point.x, point.y, paint)
        verify(paint, times(2)).strokeWidth
        verify(paint, times(2)).strokeWidth = any()
    }

    @Test
    fun testAddingAdditionalPointPassesTolerance() {
        stroke = Stroke()
        val initialPoint = CanvasPoint(0f, 0f)
        val additionalPoint = CanvasPoint(20f, 20f)
        stroke.addPoint(initialPoint.x, initialPoint.y, canvas, paint)
        stroke.addPoint(additionalPoint.x, additionalPoint.y, canvas, paint)

        assert(stroke.points.size).isEqualTo( 2)

        verify(canvas).drawLine(initialPoint.x, initialPoint.y, additionalPoint.x, additionalPoint.y, paint)
    }

    @Test
    fun testAddingAdditionalPointFailsTolerance() {
        stroke = Stroke()
        val initialPoint = CanvasPoint(0f, 0f)
        val additionalPoint = CanvasPoint(0f, 0f)
        stroke.addPoint(initialPoint.x, initialPoint.y, canvas, paint)

        verify(canvas).drawPoint(initialPoint.x, initialPoint.y, paint)
        verifyNoMoreInteractions(canvas)

        stroke.addPoint(additionalPoint.x, additionalPoint.y, canvas, paint)

        assert(stroke.points.size).isEqualTo(1)
    }
}