package miles.scribble.home.drawing

import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import assertk.assert
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isNotEqualTo
import assertk.assertions.isNotNull
import miles.redux.core.Dispatcher
import miles.redux.core.Dispatchers
import miles.redux.core.SimpleStore
import miles.redux.core.Store
import miles.scribble.home.HomeActivityEvents
import miles.scribble.home.HomeActivityEventsReducer
import miles.scribble.home.viewmodel.HomeState
import miles.scribble.home.viewmodel.HomeViewModel
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
        assert(oldState).isNotEqualTo(state)
    }

    @Test
    fun testDispatchingPictureChosenEventMutatesCorrectState() {
        dispatcher.dispatch(HomeActivityEvents.PictureChosen(RuntimeEnvironment.application.contentResolver,
                Uri.EMPTY))
        assert(state.drawType).isEqualTo(DrawType.PICTURE)
        assert(state.photoState.photoBitmap).isNotNull()
        assert(state.isMenuOpen).isFalse()
    }
}