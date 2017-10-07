package miles.scribble.home.circlemenu

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.spy
import com.nhaarman.mockito_kotlin.verify
import io.reactivex.observers.TestObserver
import miles.redux.core.Dispatchers
import miles.redux.core.SimpleStore
import miles.redux.core.Store
import miles.scribble.home.drawing.DrawType
import miles.scribble.home.drawing.redrawable.DrawHistory
import miles.scribble.home.drawing.redrawable.RedrawableLines
import miles.scribble.home.events.CircleMenuEvents
import miles.scribble.home.events.CircleMenuEventsReducer
import miles.scribble.home.viewmodel.HomeState
import miles.scribble.util.assertEquals
import miles.scribble.util.assertFalse
import miles.scribble.util.assertNotEquals
import miles.scribble.util.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
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
        store = SimpleStore(HomeState(canvas = Canvas(), bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888),
                history = spy(DrawHistory())))
        val reducer = CircleMenuEventsReducer()
        dispatcher = Dispatchers.create(store, reducer)
    }

    @Test
    fun testMenuTogglesWhenDispatchingToggleEvent() {
        dispatcher.dispatch(CircleMenuEvents.ToggleClicked(true))
        assertTrue(state.isMenuOpen)

        dispatcher.dispatch(CircleMenuEvents.ToggleClicked(false))
        assertFalse(state.isMenuOpen)
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

        assertTrue(state.drawType == DrawType.INK)
        assertEquals(state.lastX, state.bitmap.width / 2f)
        assertEquals(state.lastY, state.bitmap.height / 2f)
        assertFalse(state.isMenuOpen)
    }

    @Test
    fun testEraserClickedMutatesState() {
        CircleMenuEvents.EraserClicked(isErasing = true).let {
            dispatcher.dispatch(it)

            assertTrue(state.drawType == DrawType.ERASE)
            assertEquals(state.paint.color, state.backgroundColor)
        }

        CircleMenuEvents.EraserClicked(isErasing = false).let {
            dispatcher.dispatch(it)

            assertTrue(state.drawType == DrawType.NORMAL)
            assertEquals(state.paint.color, state.strokeColor)
        }
    }

    @Test
    fun testDispatchingInitialRedrawEvent() {
        state.history.push(RedrawableLines(listOf(), Paint()))

        dispatcher.dispatch(CircleMenuEvents.RedrawStarted(isUndo = true))
        assertFalse(state.isSafeToDraw)
        verify(state.history).undo()

        dispatcher.dispatch(CircleMenuEvents.RedrawStarted(isUndo = false))
        assertFalse(state.isSafeToDraw)
        verify(state.history).redo()
    }

    @Test
    fun testDispatchingRedrawRequestEvent() {
        state.history.push(RedrawableLines(listOf(), Paint()))

        dispatcher.dispatch(CircleMenuEvents.RedrawStarted(isUndo = true))
        dispatcher.dispatch(CircleMenuEvents.Redraw())
        verify(state.history).redraw(any())
    }

    @Test
    fun testDispatchingCompletedRedrawEvent() {
        val oldBitmap = state.bitmap
        val oldCanvas = state.canvas
        state.history.push(RedrawableLines(listOf(), Paint()))
        dispatcher.dispatch(CircleMenuEvents.RedrawStarted(isUndo = true))
        dispatcher.dispatch(CircleMenuEvents.Redraw())
        dispatcher.dispatch(CircleMenuEvents.RedrawCompleted())
        assertTrue(state.isSafeToDraw)
        assertNotEquals(oldBitmap, state.bitmap)
        assertNotEquals(oldCanvas, state.canvas)
    }
}