package miles.kodi.api

import miles.kodi.Kodi
import miles.kodi.module.Builder
import miles.kodi.module.Module
import miles.kodi.provider.LazyProvider
import miles.kodi.provider.Provider

/**
 * Created by mbpeele on 10/10/17.
 */
class KodiBuilder internal constructor(val module: Module, override val kodi: Kodi) : HasKodi {

    @Suppress("RemoveExplicitTypeArguments")
    inline fun <reified T> instance(tag: String = "") : T = kodi.instance<T>(tag)

    inline fun <reified T> bind(tag: String = "") : Builder = module.bind<T>(tag)

    infix inline fun <reified T> Builder.with(provider: Provider<T>) {
        module.providers.put(key, provider)
    }

    inline fun <reified T> provider(crossinline block: () -> T) =
            object : Provider<T> {
                override fun provide() = block.invoke()
            }

    inline fun <reified T> singleton(crossinline block: () -> T) : Provider<T> {
        val provider = provider(block)
        return LazyProvider(provider)
    }
}