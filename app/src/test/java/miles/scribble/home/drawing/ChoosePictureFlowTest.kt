package miles.scribble.home.drawing

import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import miles.scribble.home.HomeActivityEvents
import miles.scribble.home.HomeActivityEventsReducer
import miles.scribble.home.viewmodel.HomeState
import miles.scribble.home.viewmodel.HomeViewModel
import miles.scribble.redux.core.Dispatcher
import miles.scribble.redux.core.Dispatchers
import miles.scribble.redux.core.SimpleStore
import miles.scribble.redux.core.Store
import miles.scribble.util.assertEquals
import miles.scribble.util.assertFalse
import miles.scribble.util.assertNotEquals
import miles.scribble.util.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

/**
 * Created by mbpeele on 8/13/17.
 */
@RunWith(RobolectricTestRunner::class)
class ChoosePictureFlowTest {

    lateinit var store : Store<HomeState>
    lateinit var viewModel : HomeViewModel
    val state : HomeState
        get() = store.state
    lateinit var dispatcher : Dispatcher<HomeActivityEvents, HomeActivityEvents>

    @Before
    fun setup() {
        store = SimpleStore(HomeState(canvas = Canvas(), bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)))
        val reducer = HomeActivityEventsReducer()
        dispatcher = Dispatchers.create(store, reducer)
    }

    @Test
    fun testDispatchingPictureChosenEventMutatesState() {
        val oldState = state
        dispatcher.dispatch(HomeActivityEvents.PictureChosen(RuntimeEnvironment.application.contentResolver,
                Uri.EMPTY))
        assertNotEquals(oldState, state)
    }

    @Test
    fun testDispatchingPictureChosenEventMutatesCorrectState() {
        dispatcher.dispatch(HomeActivityEvents.PictureChosen(RuntimeEnvironment.application.contentResolver,
                Uri.EMPTY))
        assertEquals(state.drawType, DrawType.PICTURE)
        assertNotNull(state.photoState.photoBitmap!!) // superfluous I know
        assertFalse(state.isMenuOpen)
    }
}