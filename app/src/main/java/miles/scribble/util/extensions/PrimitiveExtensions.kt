package miles.scribble.util.extensions

/**
 * Created using mbpeele on 6/25/17.
 */
fun String.containsNewLine() : Boolean {
    return contains("\n")
}

fun Boolean.toInt() : Int {
    return if (this) 1 else 0
}

fun Int.largest(otherInt: Int) : Int {
    return if (this > otherInt) this else otherInt
}