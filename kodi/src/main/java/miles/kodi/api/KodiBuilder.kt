package miles.kodi.api

import miles.kodi.internal.BindingBuilder
import miles.kodi.provider.LazyProvider
import miles.kodi.provider.Provider
import kotlin.reflect.KClass

/**
 * Created using peelemil on 10/11/17.
 */
interface KodiBuilder {

    fun child(builder: KodiBuilder.() -> Unit)

    fun <T : Any> bind(tag: String = "", type: KClass<T>) : BindingBuilder

    infix fun <T> BindingBuilder.using(provider: Provider<T>)

    fun <T : Any> get(tag: String = "", type: KClass<T>) : T
}

inline fun <reified T : Any> KodiBuilder.bind(tag: String = "") : BindingBuilder =
    bind(tag, T::class)

inline fun <reified T : Any> KodiBuilder.get(key: String = "") = get(key, T::class)

inline fun <reified T> provider(crossinline block: () -> T) =
        object : Provider<T> {
            override fun provide() = block.invoke()
        }

inline fun <reified T> singleton(crossinline block: () -> T) : Provider<T> {
    val provider = provider(block)
    return LazyProvider(provider)
}