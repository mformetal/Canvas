package miles.kodi

import miles.kodi.api.Delinker
import miles.kodi.api.HasKodi
import miles.kodi.internal.Node
import miles.kodi.internal.NodeDelinker
import miles.kodi.module.Module
import kotlin.reflect.KClass

/**
 * Created from mbpeele on 10/7/17.
 */
class Kodi private constructor(val root: Node): HasKodi {

    override val kodi: Kodi get() = this
    private val instancer: Instancer = Instancer(this)

    companion object {
        val ROOT = Kodi::class

        fun init(block: Module.(Kodi) -> Unit) : Kodi {
            val rootModule = Module()
            val rootNode = Node(rootModule, ROOT)
            return Kodi(rootNode).apply {
                block.invoke(rootModule, this)
            }
        }
    }

    fun module(block: Module.() -> Unit) = Module().apply(block)

    fun link(parent: KClass<*>, scope: KClass<*>, init: Module.(Instancer) -> Unit) : Delinker {
        val parentNode = root.search { it.scope == parent } ?: throw IllegalArgumentException("Parent scope $scope not found.")
        val module = Module().apply {
            init.invoke(this, instancer)
        }
        val childNode = Node(module, scope)
        parentNode.addChild(childNode)
        return NodeDelinker(parentNode, childNode)
    }

    inline fun <reified T> instance(tag: String = "") : T {
        val node = root.search { it.module.exists<T>() }
                ?: throw IllegalStateException("No binding with the specified key ${T::class.simpleName + tag} exists.")
        @Suppress("RemoveExplicitTypeArguments")
        return node.module.get<T>(tag)
    }
}

class Instancer(private val kodi: Kodi) {

    inline fun <reified T> instance(tag: String = "") : T = `access$kodi`.instance(tag)

    @PublishedApi
    internal val `access$kodi`: Kodi
        get() = kodi
}