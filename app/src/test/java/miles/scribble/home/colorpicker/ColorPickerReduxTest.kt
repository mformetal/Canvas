package miles.scribble.home.colorpicker

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import miles.scribble.home.viewmodel.HomeState
import miles.scribble.redux.core.Dispatcher
import miles.scribble.redux.core.Dispatchers
import miles.scribble.redux.core.SimpleStore
import miles.scribble.redux.core.Store
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Created by mbpeele on 8/12/17.
 */
@RunWith(RobolectricTestRunner::class)
class ColorPickerReduxTest {

    lateinit var store : Store<HomeState>
    val state : HomeState
        get() = store.state
    lateinit var dispatcher : Dispatcher<ColorPickerEvents, ColorPickerEvents>

    val color = Color.RED

    @Before
    fun setup() {
        store = SimpleStore(HomeState(canvas = Canvas(), bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)))
        val reducer = ColorPickerReducer()
        dispatcher = Dispatchers.create(store, reducer)
    }

    @Test
    fun testDispatchingStrokeColorEventChangesHomeStateStrokeColor() {
        dispatcher.dispatch(ColorPickerEvents.StrokeColorChosen(color))
        Assert.assertEquals(state.strokeColor, color)
        Assert.assertFalse(state.isMenuOpen)
    }

    @Test
    fun testDispatchBackgroundColorEventChangesHomeStateBackgroundColor() {
        dispatcher.dispatch(ColorPickerEvents.BackgroundColorChosen(color))
        Assert.assertEquals(state.backgroundColor, color)
        Assert.assertFalse(state.isMenuOpen)
    }
}