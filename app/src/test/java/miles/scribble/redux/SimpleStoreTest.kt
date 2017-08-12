package miles.scribble.redux

import miles.scribble.redux.core.SimpleStore
import miles.scribble.redux.core.StateChangeListener
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner
import java.util.concurrent.CopyOnWriteArrayList

/**
 * Created by mbpeele on 8/12/17.
 */
@RunWith(MockitoJUnitRunner::class)
class SimpleStoreTest {

    @Mock lateinit var subscriber : StateChangeListener<SimpleState>

    @Test
    fun testInitialSubscribe() {
        val initialState = SimpleState(0)
        val store = SimpleStore(initialState = initialState,
                subscribers = CopyOnWriteArrayList<StateChangeListener<SimpleState>>(listOf(subscriber)))
        Mockito.verify(subscriber).onStateChanged(initialState)
        Assert.assertEquals(store.state.ordinal, initialState.ordinal)
    }

    @Test
    fun testSubscriptionCalled() {
        val initialState = SimpleState(0)
        val secondState = SimpleState(1)
        val store = SimpleStore(initialState = initialState)
        store.subscribe(subscriber)
        store.state = secondState
        Mockito.verify(subscriber).onStateChanged(secondState)
    }

    @Test
    fun testUnsubscription() {
        val initialState = SimpleState(0)
        val secondState = SimpleState(1)
        val thirdState = SimpleState(2)
        val store = SimpleStore(initialState = initialState)
        store.subscribe(subscriber)
        store.state = secondState
        Mockito.verify(subscriber).onStateChanged(secondState)
        store.unsubscribe(subscriber)
        store.state = thirdState
        Mockito.verify(subscriber, Mockito.times(0)).onStateChanged(thirdState)
    }
}