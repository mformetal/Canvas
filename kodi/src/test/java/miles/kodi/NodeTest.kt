package miles.kodi

import miles.kodi.internal.Node
import miles.kodi.module.Module
import org.junit.Test

/**
 * Created from mbpeele on 10/7/17.
 */
class NodeTest {

    @Test
    fun testAddingChildNodes() {
        val root = Node(Module(), Node::class)
        val child = Node(Module(), Node::class)
        root.addChild(child)

        assert(child.parent == root)
        assert(root.children.contains(child))
    }

    @Test
    fun testRemovingChildNodes() {
        val root = Node(Module(), Node::class)
        val child = Node(Module(), Node::class)
        root.addChild(child)
        root.removeChild(child)

        assert(child.parent == null)
        assert(!root.children.contains(child))
    }

    @Test(expected = IllegalArgumentException::class)
    fun testRemovingChildWithoutValidParent() {
        val root = Node(Module(), Node::class)
        val child = Node(Module(), Node::class)
        root.removeChild(child)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testAddingChildNodeToItself() {
        val node = Node(Module(), Node::class)
        node.addChild(node)
    }
}