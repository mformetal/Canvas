package miles.kodi.provider

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