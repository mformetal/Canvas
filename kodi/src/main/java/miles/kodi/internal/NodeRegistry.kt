package miles.kodi.internal

import miles.kodi.Kodi
import miles.kodi.api.ScopeRegistry

/**
 * Created by mbpeele on 10/7/17.
 */
internal class NodeRegistry(private val parent: Node, private val child: Node) : ScopeRegistry {

    override fun unregister() {
        parent.removeChild(child)
    }
}