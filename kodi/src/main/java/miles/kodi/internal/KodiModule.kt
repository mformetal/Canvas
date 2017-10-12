package miles.kodi.internal

import miles.kodi.api.KodiBuilder
import kotlin.reflect.KClass

/**
 * Created by peelemil on 10/11/17.
 */
internal class KodiModule(internal val nodeOfModule: Node,
                          internal val module: Module = Module()) : KodiBuilder by module {

    override fun <T : Any> get(tag: String, type: KClass<T>): T {
        val key = type.key(tag)
        val node = nodeOfModule.searchParents { it.module.providers.contains(key) }
        @Suppress("FoldInitializerAndIfToElvis")
        if (node == null) {
            throw IllegalStateException("No binding with key $key exists for scope ${nodeOfModule.scope}.")
        }
        return node.module.get(tag, type)
    }
}