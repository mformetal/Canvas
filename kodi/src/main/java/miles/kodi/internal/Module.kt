package miles.kodi.internal

import miles.kodi.api.KodiBuilder
import miles.kodi.provider.Provider
import kotlin.reflect.KClass

/**
 * Created from mbpeele on 10/7/17.
 */
internal class Module : KodiBuilder {

    @Suppress("MemberVisibilityCanPrivate")
    internal val providers: HashMap<String, Provider<*>> = HashMap()

    override fun child(builder: KodiBuilder.() -> Unit) {
        val module = Module().apply(builder)
        providers.putAll(module.providers)
    }

    override fun <T : Any> bind(tag: String, type: KClass<T>) : BindingBuilder<T> {
        if (providers.keys.contains(type.simpleName) && tag.isEmpty()) {
            throw AmbiguousBindingException()
        } else if (providers.keys.contains(type.simpleName + tag)) {
            throw DuplicateBindingException()
        }

        val key = type.key(tag)
        return BindingBuilder(key)
    }

    override fun <T> BindingBuilder<T>.using(provider: Provider<T>) {
        providers.put(key, provider)
    }

    override fun <T: Any> get(tag: String, type: KClass<T>): T {
        val key = type.key(tag)
        @Suppress("UNCHECKED_CAST")
        return providers[key]!!.provide() as T
    }

    fun KClass<*>.key(tag: String) = simpleName + tag
}

internal fun module(block: Module.() -> Unit) = Module().apply(block)

class BindingBuilder<T>(val key: String)