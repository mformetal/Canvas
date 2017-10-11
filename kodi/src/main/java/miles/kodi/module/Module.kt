package miles.kodi.module

import miles.kodi.provider.LazyProvider
import miles.kodi.provider.Provider

/**
 * Created from mbpeele on 10/7/17.
 */
internal class Module {

    @PublishedApi
    internal val providers: HashMap<String, Provider<*>> = HashMap()

    fun submodule(block: Module.() -> Unit) {
        val module = Module().apply(block)
        providers.putAll(module.providers)
    }

    inline fun <reified T> bind(tag: String = "") : Builder {
        if (providers.keys.contains(T::class.simpleName) && tag.isEmpty()) {
            throw IllegalStateException("Module cannot contain dependencies of the same class without specifying a Tag.")
        } else if (providers.keys.contains(T::class.simpleName + tag)) {
            throw IllegalStateException("Module cannot contain two dependencies with the same Key.")
        }

        val key = T::class.simpleName + tag
        return Builder(key)
    }

    infix inline fun <reified T> Builder.from(provider: Provider<T>) {
        providers.put(key, provider)
    }

    inline fun <reified T> exists(tag: String = "") : Boolean = providers.containsKey(T::class.simpleName + tag)

    @PublishedApi
    internal inline fun <reified T> get(tag: String = "") : T = providers[T::class.simpleName + tag]!!.provide() as T

    inline fun <reified T> provider(crossinline block: () -> T) =
            object : Provider<T> {
                override fun provide() = block.invoke()
            }

    inline fun <reified T> singleton(crossinline block: () -> T) : Provider<T> {
        val provider = provider(block)
        return LazyProvider(provider)
    }
}

class Builder(val key: String)