package miles.kodi.provider

/**
 * Created by mbpeele on 10/7/17.
 */
interface Provider<out T> {

    fun provide() : T
}

class CachedProvider<T>(private val provider: Provider<T>) : Provider<T> {

    private val value by lazy { provider.provide() }

    override fun provide(): T = value

}

class FactoryProvider<T>(private val varargs: Any, private val creator: (Any) -> T) : Provider<T> {

    override fun provide(): T = creator.invoke(varargs)

}

fun <T> provide(block: () -> T) =
        object : Provider<T> {
            override fun provide() = block.invoke()
        }