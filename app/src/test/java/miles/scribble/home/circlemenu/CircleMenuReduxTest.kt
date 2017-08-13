package miles.scribble.home.circlemenu

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import io.reactivex.observers.TestObserver
import io.reactivex.subscribers.TestSubscriber
import miles.scribble.home.drawing.DrawType
import miles.scribble.home.events.CircleMenuEvents
import miles.scribble.home.events.CircleMenuEventsReducer
import miles.scribble.home.events.ColorPickerEvents
import miles.scribble.home.events.ColorPickerReducer
import miles.scribble.home.viewmodel.HomeState
import miles.scribble.redux.core.*
import miles.scribble.util.assertEquals
import miles.scribble.util.assertFalse
import miles.scribble.util.assertTrue
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner
import org.robolectric.RobolectricTestRunner

/**
 * Created by mbpeele on 8/12/17.
 */
@RunWith(RobolectricTestRunner::class)
class CircleMenuReduxTest {

    lateinit var store : Store<HomeState>
    val state : HomeState
        get() = store.state
    lateinit var dispatcher : Dispatcher<CircleMenuEvents, CircleMenuEvents>

    @Before
    fun setup() {
        store = SimpleStore(HomeState(canvas = Canvas(), bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)))
        val reducer = CircleMenuEventsReducer()
        dispatcher = Dispatchers.create(store, reducer)
    }

    @Test
    fun testMenuTogglesWhenDispatchingToggleEvent() {
        dispatcher.dispatch(CircleMenuEvents.ToggleClicked(true))
        Assert.assertTrue(state.isMenuOpen)

        dispatcher.dispatch(CircleMenuEvents.ToggleClicked(false))
        Assert.assertFalse(state.isMenuOpen)
    }

    @Test
    fun testBrushClickedEmitsEvent() {
        val event = CircleMenuEvents.BrushClicked()
        val testSubscriber = TestObserver<Event>()
        state.onClickSubject.subscribe(testSubscriber)
        dispatcher.dispatch(event)
        testSubscriber.assertValue(event)
    }

    @Test
    fun testPictureClickedEmitsEvent() {
        val event = CircleMenuEvents.PictureClicked()
        val testSubscriber = TestObserver<Event>()
        state.onClickSubject.subscribe(testSubscriber)
        dispatcher.dispatch(event)
        testSubscriber.assertValue(event)
    }

    @Test
    fun testStrokeClickedEmitsEvent() {
        val event = CircleMenuEvents.StrokeColorClicked()
        val testSubscriber = TestObserver<Event>()
        state.onClickSubject.subscribe(testSubscriber)
        dispatcher.dispatch(event)
        testSubscriber.assertValue(event)
    }

    @Test
    fun testBackgroundClickedEmitsEvent() {
        val event = CircleMenuEvents.BackgroundColorClicked()
        val testSubscriber = TestObserver<Event>()
        state.onClickSubject.subscribe(testSubscriber)
        dispatcher.dispatch(event)
        testSubscriber.assertValue(event)
    }

    @Test
    fun testInkClickedMutatesState() {
        val event = CircleMenuEvents.InkClicked()
        dispatcher.dispatch(event)

        assertTrue(state.drawType is DrawType.Ink)
        assertEquals(state.lastX, state.bitmap.width / 2f)
        assertEquals(state.lastY, state.bitmap.height / 2f)
        assertFalse(state.isMenuOpen)
    }

    @Test
    fun testEraserClickedMutatesState() {
        CircleMenuEvents.EraserClicked(isErasing = true).let {
            dispatcher.dispatch(it)

            assertTrue(state.drawType is DrawType.Erase)
            assertEquals(state.paint.color, state.backgroundColor)
        }

        CircleMenuEvents.EraserClicked(isErasing = false).let {
            dispatcher.dispatch(it)

            assertTrue(state.drawType is DrawType.Normal)
            assertEquals(state.paint.color, state.strokeColor)
        }
    }
}