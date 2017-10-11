package miles.kodi

import miles.kodi.api.*
import miles.kodi.internal.KodiModule
import miles.kodi.internal.Node
import miles.kodi.internal.NodeRegistry
import miles.kodi.internal.Module

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
}

internal class KodiScopeBuilder(private val kodi: Kodi) : ScopeBuilder {

    var parent : Node = kodi.root
    lateinit var childNode : Node
    lateinit var childScope : Scope
    lateinit var kodiModule: KodiModule

    override fun dependsOn(scope: Scope) : ScopeBuilder {
        parent = kodi.root.search { it.scope == scope }
            ?: throw IllegalArgumentException("No matching scope $scope exists.")
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