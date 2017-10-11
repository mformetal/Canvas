package miles.kodi

import miles.kodi.api.HasKodi
import miles.kodi.api.KodiBuilder
import miles.kodi.api.ScopeRegistry
import miles.kodi.internal.Node
import miles.kodi.internal.NodeRegistry
import miles.kodi.module.Module
import kotlin.reflect.KClass

/**
 * Created from mbpeele on 10/7/17.
 */
class Kodi private constructor(internal val root: Node): HasKodi {

    override val kodi: Kodi get() = this

    @PublishedApi
    internal val accessRoot : Node
        get() = root

    @PublishedApi
    internal val accessNodeRegistry = { parent: Node, child: Node ->
        NodeRegistry(parent, child)
    }

    companion object {
        fun init(block: Module.(Kodi) -> Unit) : Kodi {
            val rootModule = Module()
            val rootNode = Node(rootModule, Kodi::class)
            return Kodi(rootNode).apply {
                block.invoke(rootModule, this)
            }
        }
    }

    inline fun <reified T> rootScope(block: KodiBuilder.() -> Unit) : ScopeRegistry {
        val builder = KodiBuilder(Module(), this).apply(block)
        val node = Node(builder.module, T::class)
        accessRoot.addChild(node)
        return accessNodeRegistry.invoke(accessRoot, node)
    }

    inline fun <reified T> dependingScope(dependsOn: KClass<*>, block: Module.() -> Unit) : ScopeRegistry {
        val parent = accessRoot.search { it.scope == dependsOn } ?: throw IllegalArgumentException("Parent scope ${dependsOn.qualifiedName} not found.")
        val module = Module().apply(block)
        val node = Node(module, T::class)
        parent.addChild(node)
        return accessNodeRegistry.invoke(parent, node)
    }

    inline fun <reified T> instance(tag: String = "") : T {
        val node = accessRoot.search { it.module.exists<T>() }
                ?: throw IllegalStateException("No binding with the specified key ${T::class.simpleName + tag}")
        @Suppress("RemoveExplicitTypeArguments")
        return node.module.get<T>(tag)
    }
}