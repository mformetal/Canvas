package miles.kodi

import miles.kodi.api.HasKodi
import miles.kodi.api.Unbinder
import miles.kodi.internal.Link
import miles.kodi.internal.Node
import miles.kodi.module.Module
import miles.kodi.provider.LazyProvider
import miles.kodi.provider.Provider
import kotlin.reflect.KClass

/**
 * Created by mbpeele on 10/7/17.
 */
class Kodi : HasKodi {

    override val kodi: Kodi get() = this

    companion object {
        val ROOT = Kodi::class
    }

    lateinit var root: Node

    fun root(module: Module) {
        root = Node(module, ROOT)
    }

    fun root(block: Module.() -> Unit) {
        val module = Module().apply(block)
        root = Node(module, ROOT)
    }

    fun link(parent: KClass<*>, scope: KClass<*>, module: Module) : Unbinder {
        val parentNode = root.search { it.scope == parent } ?: throw IllegalArgumentException("Parent scope $scope not found.")
        val childNode = Node(module, scope)
        parentNode.addChild(childNode)
        return Link(parentNode, childNode)
    }

    inline fun <reified T> instance(tag: String = "") : T {
        val key = T::class.simpleName + tag
        val node = root.search { it.module.providers.contains(key) }
                ?: throw IllegalStateException("No binding with the specified key ${T::class.simpleName + tag} exists.")
        @Suppress("RemoveExplicitTypeArguments")
        return node.module.get<T>()
    }
}

fun kodi(block: Kodi.() -> Unit) = Kodi().apply(block)

inline fun <T> HasKodi.provider(crossinline block: () -> T) =
        object : Provider<T> {
            override fun provide() = block.invoke()
        }

inline fun <T> HasKodi.singleton(crossinline block: () -> T) : Provider<T> {
    val provider = provider(block)
    return LazyProvider(provider)
}

inline fun <T> HasKodi.factory(crossinline block: HasKodi.() -> T) : Provider<T> {
    return object : Provider<T> {
        override fun provide(): T = block.invoke(kodi)
    }
}