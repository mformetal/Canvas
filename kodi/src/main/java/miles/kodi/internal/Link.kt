package miles.kodi.internal

import miles.kodi.api.Unbinder

/**
 * Created by mbpeele on 10/7/17.
 */
class Link(private val parent: Node, private val child: Node) : Unbinder {

    override fun unbind() {
        parent.removeChild(child)
    }
}