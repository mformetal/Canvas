package miles.kodi

import miles.kodi.api.*
import miles.kodi.internal.*
import kotlin.reflect.KClass

/**
 * Created by peelemil on 10/11/17.
 */
class Kodi private constructor(internal val root: Node) {

    companion object {
        internal val ROOT_SCOPE = scoped<Kodi>()

        fun init(builder: KodiBuilder.() -> Unit) : Kodi {
            val module = Module().apply(builder)
            val rootNode = Node(module, ROOT_SCOPE)
            return Kodi(rootNode)
        }
    }

    fun scope(builder: ScopeBuilder.() -> Unit) : ScopeRegistry {
        val scopeBuilder = KodiScopeBuilder(this).apply(builder)
        return NodeRegistry(scopeBuilder.parent, scopeBuilder.childNode)
    }

    fun <T : Any> instance(scope: Scope, key: String = "") : T {
        val child = root.search { it.scope == scope } ?: throw NoMatchingScopeException(scope)
        val result = child.searchUpToRoot { it.module.providers.containsKey(key) } ?: throw NoMatchingKeyException(key)
        @Suppress("UNCHECKED_CAST")
        return result.module.providers[key]!!.provide() as T
    }

    fun <T : Any> instance(scope: Scope, kClass: KClass<T>, tag: String = "") : T = instance(scope, kClass.key(tag))

    inline fun <reified T : Any> get(scope: Scope, tag: String = "") : T = instance(scope, T::class.key(tag))
}

internal class KodiScopeBuilder(private val kodi: Kodi) : ScopeBuilder {

    var parent : Node = kodi.root
    lateinit var childNode : Node
    lateinit var childScope : Scope
    lateinit var kodiModule: KodiModule

    override fun dependsOn(scope: Scope) : ScopeBuilder {
        parent = kodi.root.search { it.scope == scope } ?: throw NoMatchingScopeException(scope)
        return this
    }

    override fun with(scope: Scope) : ScopeBuilder {
        childScope = scope
        return this
    }

    override fun build(block: KodiBuilder.() -> Unit) : ScopeBuilder {
        val module = Module()
        childNode = Node(module, childScope)
        parent.addChild(childNode)
        kodiModule = KodiModule(childNode, module).apply(block)
        return this
    }
}