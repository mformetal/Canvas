package miles.scribble.util

import org.mockito.Mockito

/**
 * Created by mbpeele on 8/12/17.
 */
inline fun <reified T : Any> mock(): T = Mockito.mock(T::class.java) as T

fun <T> any(): T {
    Mockito.any<T>()
    return uninitialized()
}

private fun <T> uninitialized(): T = null as T