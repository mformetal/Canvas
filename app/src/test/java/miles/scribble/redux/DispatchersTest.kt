package miles.scribble.redux

import miles.scribble.redux.core.Dispatchers
import miles.scribble.redux.core.Reducer
import miles.scribble.redux.core.SimpleStore
import miles.scribble.util.mock
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner
import miles.scribble.util.any

/**
 * Created by mbpeele on 8/12/17.
 */
@RunWith(MockitoJUnitRunner::class)
class DispatchersTest {

    @Mock lateinit var mockReducer : Reducer<SimpleEvents, SimpleState>
    
    @Test
    fun testDispatchingCallsReducer() {
        val state = SimpleState(0)
        Mockito.`when`(mockReducer.reduce(any(), any())).thenReturn(state)
        val store = SimpleStore(state)
        val dispatcher = Dispatchers.create(store, mockReducer)
        dispatcher.dispatch(SimpleEvents.EventOne())
        Mockito.verify(mockReducer).reduce(any(), any())
    }

    @Test
    fun testDispatchingChangesStoreState() {
        val state = SimpleState(0)
        val secondState = SimpleState(1)
        Mockito.`when`(mockReducer.reduce(any<SimpleEvents.EventOne>(), any())).thenReturn(secondState)
        val store = SimpleStore(state)
        val dispatcher = Dispatchers.create(store, mockReducer)
        dispatcher.dispatch(SimpleEvents.EventOne())
        Assert.assertEquals(store.state.ordinal, 1)
    }
}