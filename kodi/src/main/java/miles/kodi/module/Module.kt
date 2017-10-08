package miles.kodi.module

import miles.kodi.provider.LazyProvider
import miles.kodi.provider.Provider

/**
 * Created by mbpeele on 10/7/17.
 */
class Module {

    val providers: HashMap<String, Provider<*>> = HashMap()

    fun submodule(module: Module) {
        providers.putAll(module.providers)
    }

    inline fun <reified T> bind(tag: String = "") : Binding<T> {
        if (providers.keys.contains(T::class.simpleName) && tag.isEmpty()) {
            throw IllegalStateException("Module cannot contain dependencies of the same class without specifying a Tag.")
        }

        return Binding(T::class.simpleName + tag)
    }

    infix inline fun <reified T> Binding<T>.by(provider: Provider<T>) {
        this.provider = provider
        providers.put(key, this)
    }

    inline fun <reified T> get(tag: String = "") : T {
        val binding = providers[T::class.simpleName + tag]
                ?: throw IllegalStateException("No binding with the specified key ${T::class.simpleName + tag} exists.")
        return binding.provide() as T
    }
}

fun module(block: Module.() -> Unit) = Module().apply(block)

inline fun <T> Module.provider(crossinline block: () -> T) =
        object : Provider<T> {
            override fun provide() = block.invoke()
        }

inline fun <T> Module.singleton(crossinline block: () -> T) : Provider<T> {
    val provider = provider(block)
    return LazyProvider(provider)
}

inline fun <T> Module.factory(crossinline block: Module.() -> T) : Provider<T> {
    return object : Provider<T> {
        override fun provide(): T = block.invoke(this@factory)
    }
}