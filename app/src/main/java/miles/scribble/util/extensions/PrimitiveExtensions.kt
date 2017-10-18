package miles.scribble.util.extensions

/**
 * Created using mbpeele on 6/25/17.
 */
fun Boolean.toInt() : Int {
    return if (this) 1 else 0
}

infix fun Int.larger(otherInt: Int) : Int {
    return if (this > otherInt) this else otherInt
}

fun smallerOf(first: Int, second: Int) : Int {
    return if (first > second) second else first
}

