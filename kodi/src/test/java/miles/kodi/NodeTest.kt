package miles.kodi

import miles.kodi.internal.Node
import miles.kodi.module.module
import org.junit.Test

/**
 * Created by mbpeele on 10/7/17.
 */
class NodeTest {

    @Test
    fun testAddingChildNodes() {
        val root = Node(module {  }, Node::class)
        val child = Node(module {  }, Node::class)
        root.addChild(child)

        assert(child.parent == root)
        assert(root.children.contains(child))
    }

    @Test
    fun testRemovingChildNodes() {
        val root = Node(module {  }, Node::class)
        val child = Node(module {  }, Node::class)
        root.addChild(child)
        root.removeChild(child)

        assert(child.parent == null)
        assert(!root.children.contains(child))
    }

    @Test(expected = IllegalArgumentException::class)
    fun testRemovingChildWithoutValidParent() {
        val root = Node(module {  }, Node::class)
        val child = Node(module {  }, Node::class)
        root.removeChild(child)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testAddingChildNodeToItself() {
        val node = Node(module {  }, Node::class)
        node.addChild(node)
    }
}