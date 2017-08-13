package miles.scribble.util

import org.junit.Assert
import org.mockito.Mockito

/**
 * Created by mbpeele on 8/12/17.
 */
fun assertFalse(boolean: Boolean) {
    Assert.assertFalse(boolean)
}

fun assertTrue(boolean: Boolean) {
    Assert.assertTrue(boolean)
}

fun assertEquals(first: Any, second: Any) {
    Assert.assertEquals(first, second)
}