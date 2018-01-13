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

fun <A, B, C> doubleIterate(first: Collection<A>, second: Collection<B>,
                            transformer: (A, B) -> C) : List<C> {
    if (first.size != second.size) {
        throw IllegalArgumentException("Argument collections passed to doubleIterate must be same size")
    }

    val firstIterator = first.iterator()
    val secondIterator = second.iterator()
    val resultingCollection = ArrayList<C>(first.size)

    while (firstIterator.hasNext() && secondIterator.hasNext()) {
        val firstElement = firstIterator.next()
        val secondElement = secondIterator.next()
        val transformedElement = transformer.invoke(firstElement, secondElement)
        resultingCollection.add(transformedElement)
    }
    return resultingCollection.toList()
}

fun <A, B, C> doubleIterate(first: Array<A>, second: Array<B>,
                            transformer: (A, B) -> C) : List<C> {
    if (first.size != second.size) {
        throw IllegalArgumentException("Argument collections passed to doubleIterate must be same size")
    }

    val firstIterator = first.iterator()
    val secondIterator = second.iterator()
    val resultingCollection = ArrayList<C>(first.size)

    while (firstIterator.hasNext() && secondIterator.hasNext()) {
        val firstElement = firstIterator.next()
        val secondElement = secondIterator.next()
        val transformedElement = transformer.invoke(firstElement, secondElement)
        resultingCollection.add(transformedElement)
    }
    return resultingCollection.toList()
}

