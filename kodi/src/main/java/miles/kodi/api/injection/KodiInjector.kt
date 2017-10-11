package miles.kodi.api.injection

import miles.kodi.Kodi
import miles.kodi.api.Scope
import miles.kodi.internal.InjectNotCalledException
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KClass
import kotlin.reflect.KProperty

/**
 * Created by peelemil on 10/11/17.
 */
class KodiInjector(val scope: Scope) : InjectionRegistry {

    private val injections : MutableList<KodiInjection<*>> = mutableListOf()

    override fun <T : Any> register(type: KClass<T>, tag: String): ReadOnlyProperty<Any, T> {
        val key = type.simpleName + tag
        val injection = KodiInjection<T>(key)
        injections.add(injection)
        return injection
    }

    override fun inject(kodi: Kodi) {
        injections.forEach { it.provide(kodi, scope) }
    }

    private class KodiInjection<T>(private val key: String) : ReadOnlyProperty<Any, T> {

        var value : T ?= null

        override fun getValue(thisRef: Any, property: KProperty<*>): T = value ?: throw InjectNotCalledException()

        fun provide(kodi: Kodi, scope: Scope) {
            value = kodi.instance(scope, key)
        }
    }
}