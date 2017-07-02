package miles.scribble.util.extensions

import java.util.*

/**
 * Created by mbpeele on 6/28/17.
 */
fun <T> Stack<T>.copy() : Stack<T> {
    val receiver = this
    return Stack<T>().apply {
        receiver.forEach {
            push(it)
        }
    }
}