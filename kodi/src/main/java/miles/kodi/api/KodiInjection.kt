package miles.kodi.api

/**
 * Created by mbpeele on 10/8/17.
 */
inline fun <reified T> inject(hasKodi: HasKodi, name: String = "") : Lazy<T> = lazy { hasKodi.kodi.instance<T>(name) }

