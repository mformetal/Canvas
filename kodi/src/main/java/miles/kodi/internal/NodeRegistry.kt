package miles.kodi.internal

import miles.kodi.api.Scope
import miles.kodi.api.ScopeRegistry

/**
 * Created using mbpeele on 10/7/17.
 */
internal class NodeRegistry(private val parent: Node, private val child: Node) : ScopeRegistry {

    override val scope: Scope
        get() = child.scope

    override fun unregister() {
        parent.removeChild(child)
    }
}