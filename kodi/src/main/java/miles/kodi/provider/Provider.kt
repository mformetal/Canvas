package miles.kodi.provider

import miles.kodi.api.HasKodi

/**
 * Created from mbpeele on 10/7/17.
 */
interface Provider<out T> {

    fun provide() : T
}

class LazyProvider<out T>(private val provider: Provider<T>) : Provider<T> {

    private val value by lazy { provider.provide() }

    override fun provide(): T = value

}

inline fun <reified T> HasKodi.provider(crossinline block: HasKodi.() -> T) =
        object : Provider<T> {
            override fun provide() = block.invoke(kodi)
        }

inline fun <reified T> HasKodi.singleton(crossinline block: HasKodi.() -> T) : Provider<T> {
    val provider = provider(block)
    return LazyProvider(provider)
}