package miles.kodi.module

import miles.kodi.provider.Provider

/**
 * Created by mbpeele on 10/7/17.
 */
class Binding<T>(val key: String) : Provider<T> {

    lateinit var provider : Provider<T>

    override fun provide(): T = provider.provide()
}