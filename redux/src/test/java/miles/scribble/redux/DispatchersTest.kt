package miles.scribble.redux

import assertk.assert
import assertk.assertions.isEqualTo
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import miles.redux.core.Dispatchers
import miles.redux.core.Reducer
import miles.redux.core.SimpleStore
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner

/**
 * Created by mbpeele on 8/12/17.
 */
@RunWith(MockitoJUnitRunner::class)
class DispatchersTest {

    @Mock lateinit var mockReducer : Reducer<SimpleEvents, SimpleState>
    
    @Test
    fun testDispatchingCallsReducer() {
        val state = SimpleState(0)
        whenever(mockReducer.reduce(any(), any())).thenReturn(state)
        val store = SimpleStore(state)
        val dispatcher = Dispatchers.create(store, mockReducer)
        dispatcher.dispatch(SimpleEvents.EventOne())
        verify(mockReducer).reduce(any(), any())
    }

    @Test
    fun testDispatchingChangesStoreState() {
        val state = SimpleState(0)
        val secondState = SimpleState(1)
        whenever(mockReducer.reduce(any<SimpleEvents.EventOne>(), any())).thenReturn(secondState)
        val store = SimpleStore(state)
        val dispatcher = Dispatchers.create(store, mockReducer)
        dispatcher.dispatch(SimpleEvents.EventOne())
        assert(store.state.ordinal).isEqualTo(1)
    }
}