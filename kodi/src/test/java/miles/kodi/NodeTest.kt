package miles.kodi

import miles.kodi.api.Scope
import miles.kodi.internal.Module
import miles.kodi.internal.Node
import miles.kodi.internal.NodeRegistry
import org.junit.Test
import assertk.assert
import assertk.assertions.contains
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isNull

/**
 * Created from mbpeele on 10/7/17.
 */
class NodeTest {

    @Test
    fun testAddingChildNodes() {
        val root = testNode()
        val child = testNode()
        root.addChild(child)

        assert(child.parent == root)
        assert(root.children.contains(child))
    }

    @Test
    fun testRemovingChildNodes() {
        val root = testNode()
        val child = testNode()
        root.addChild(child)
        root.removeChild(child)

        assert(child.parent == null)
        assert(!root.children.contains(child))
    }

    @Test(expected = IllegalArgumentException::class)
    fun testRemovingChildWithoutValidParent() {
        val root = testNode()
        val child = testNode()
        root.removeChild(child)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testAddingChildNodeToItself() {
        val node = testNode()
        node.addChild(node)
    }

    @Test
    fun testUnregisteringRegistryRemovesChildFromParent() {
        val parent = testNode()
        val child = testNode()
        parent.addChild(child)
        NodeRegistry(parent, child).unregister()
        assert(child.parent).isNull()
        assert(parent.children).isEmpty()
    }

    private fun testNode() = Node(Module(), Scope(Node::class))
}