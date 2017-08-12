package miles.scribble.redux

import io.reactivex.subscribers.TestSubscriber
import miles.scribble.redux.core.SimpleStore
import miles.scribble.redux.core.Store
import miles.scribble.redux.rx.flowable
import miles.scribble.util.any
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner

/**
 * Created by mbpeele on 8/12/17.
 */
@RunWith(MockitoJUnitRunner::class)
class FlowableListenerTest {

    @Mock lateinit var store : Store<SimpleState>

    @Test
    fun testCreatingFlowableDoesNotSubscribeToStore() {
        flowable(store)
        Mockito.verifyZeroInteractions(store)
    }

    @Test
    fun testFlowableSubscribesToStore() {
        flowable(store).subscribe()
        Mockito.verify(store).subscribe(any())
    }

    @Test
    fun testFlowableReceivesStoreUpdates() {
        val simpleStore = SimpleStore(SimpleState(0))
        val state = SimpleState(1)
        val testSubscriber = TestSubscriber<SimpleState>()
        flowable(simpleStore).subscribe(testSubscriber)
        simpleStore.state = state
        testSubscriber.assertValueCount(1)
        testSubscriber.assertValue(state)
    }

    @Test
    fun testDisposingFlowableStopsStoreUpdates() {
        val simpleStore = SimpleStore(SimpleState(0))
        val state = SimpleState(1)
        val testSubscriber = TestSubscriber<SimpleState>()
        flowable(simpleStore).subscribe(testSubscriber)
        simpleStore.state = state
        testSubscriber.assertValueCount(1)
        testSubscriber.assertValue(state)

        testSubscriber.dispose()
        simpleStore.state = SimpleState(2)
        testSubscriber.assertValueCount(1)
        assert(testSubscriber.isCancelled)
        assert(testSubscriber.isTerminated)
    }
}