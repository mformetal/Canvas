package miles.kodi.internal

import miles.kodi.module.Module
import java.util.*
import kotlin.reflect.KClass

/**
 * Created from mbpeele on 10/7/17.
 */
@PublishedApi
internal class Node(
        val module: Module,
        var scope: KClass<*>,
        var parent: Node?= null,
        var children: MutableList<Node> = mutableListOf()) {

    fun addChild(node: Node) {
        if (node == this) {
            throw IllegalArgumentException("Cannot cyclically add $node to itself.")
        }

        node.parent = this
        children.add(node)
    }

    fun removeChild(node: Node) {
        if (!children.contains(node) || node.parent != this) {
            throw IllegalArgumentException("Node $node is not a child of $this")
        }

        node.parent = null
        children.remove(node)
    }

    fun search(predicate: (Node) -> Boolean) : Node? {
        val stack = Stack<Node>()
        stack.push(this)

        while (stack.isNotEmpty()) {
            val node = stack.pop()
            if (predicate.invoke(node)) {
                return node
            } else {
                stack.addAll(node.children)
            }
        }

        return null
    }
}