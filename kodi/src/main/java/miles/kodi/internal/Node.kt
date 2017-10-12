package miles.kodi.internal

import miles.kodi.api.Scope
import java.util.*

/**
 * Created from mbpeele on 10/7/17.
 */
internal class Node(
        val module: Module,
        val scope: Scope,
        var parent: Node? = null,
        val children: MutableList<Node> = mutableListOf()) {

    fun addChild(node: Node) {
        if (node == this) {
            throw CyclicalNodeAdditionException(node)
        }

        node.parent = this
        children.add(node)
    }

    fun removeChild(node: Node) {
        if (!children.contains(node) || node.parent != this) {
            throw RemovingNonChildNodeException(node, this)
        }

        node.parent = null
        children.remove(node)
    }

    fun searchUpToRoot(predicate: (Node) -> Boolean) : Node? {
        if (predicate.invoke(this)) {
            return this
        } else {
            if (parent != null) {
                var parent = this.parent
                while (parent != null) {
                    if (predicate.invoke(parent)) {
                        return parent
                    }
                    parent = parent.parent
                }
            }
        }

        return null
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