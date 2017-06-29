package miles.scribble.util.extensions

/**
 * Created by mbpeele on 6/28/17.
 */
fun <T> List<T>.add(element: T) : List<T> {
    return toMutableList().let { add(element) }.toList()
}