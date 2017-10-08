package miles.kodi.internal

import miles.kodi.api.Delinker

/**
 * Created by mbpeele on 10/7/17.
 */
internal class NodeDelinker(private val parent: Node, private val child: Node) : Delinker {

    override fun delink() {
        parent.removeChild(child)
    }
}