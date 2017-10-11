package miles.scribble.home.drawing

import assertk.assert
import assertk.assertions.isEqualTo
import miles.scribble.util.extensions.DateExtensions
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

/**
 * Created using mbpeele on 8/12/17.
 */
@RunWith(MockitoJUnitRunner::class)
class CanvasPointTest {

    val now = DateExtensions.currentTimeInMillis
    val firstX = 5.0
    val firstY = 5.0
    val secondX = 10.0
    val secondY = 10.0
    val distance = Math.sqrt(Math.pow(secondX - firstX, 2.0) +
                    Math.pow(secondY - firstY, 2.0)).toFloat()
    val firstPoint = CanvasPoint(firstX.toFloat(), firstY.toFloat(), time = now)
    val secondPoint = CanvasPoint(secondX.toFloat(), secondY.toFloat(), time = now + 1000)
    val duration = Math.abs(firstPoint.time - secondPoint.time)

    @Test
    fun testExpectedDistance() {
        assert(firstPoint.computeDistance(secondPoint)).isEqualTo(distance)
    }

    @Test
    fun testExpectedVelocity() {
        assert(distance / duration).isEqualTo(firstPoint.computeVelocity(secondPoint))
    }
}