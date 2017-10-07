package miles.scribble.redux

import assertk.assert
import assertk.assertions.isEqualTo
import com.nhaarman.mockito_kotlin.times
import com.nhaarman.mockito_kotlin.verify
import miles.redux.core.SimpleStore
import miles.redux.core.StateChangeListener
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
                subscribers = CopyOnWriteArrayList(listOf(subscriber)))
        Mockito.verify(subscriber).onStateChanged(initialState)
        assert(store.state.ordinal).isEqualTo(initialState.ordinal)
    }

    @Test
    fun testSubscriptionCalled() {
        val initialState = SimpleState(0)
        val secondState = SimpleState(1)
        val store = SimpleStore(initialState = initialState)
        store.subscribe(subscriber)
        store.state = secondState
        verify(subscriber).onStateChanged(secondState)
    }

    @Test
    fun testUnsubscription() {
        val initialState = SimpleState(0)
        val secondState = SimpleState(1)
        val thirdState = SimpleState(2)
        val store = SimpleStore(initialState = initialState)
        store.subscribe(subscriber)
        store.state = secondState
        verify(subscriber).onStateChanged(secondState)
        store.unsubscribe(subscriber)
        store.state = thirdState
        verify(subscriber, times(0)).onStateChanged(thirdState)
    }
}