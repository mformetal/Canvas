package miles.kodi.api.injection

import miles.kodi.Kodi
import miles.kodi.api.Scope
import miles.kodi.internal.InjectNotCalledException
import miles.kodi.internal.key
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KClass
import kotlin.reflect.KProperty

/**
 * Created by peelemil on 10/11/17.
 */
class KodiInjector(val scope: Scope) : InjectionRegistry {

    private val injectionProperties: MutableSet<InjectionProperty<*>> = mutableSetOf()

    override fun <T : Any> register(type: KClass<T>, tag: String): ReadOnlyProperty<Any, T> {
        val key = type.key(tag)
        val injection = InjectionProperty<T>(key)
        injectionProperties.add(injection)
        return injection
    }

    override fun inject(kodi: Kodi) {
        injectionProperties.forEach { it.provide(kodi, scope) }
        injectionProperties.clear()
    }

    private class InjectionProperty<T>(private val key: String) : ReadOnlyProperty<Any, T> {

        var value : T ?= null

        override fun getValue(thisRef: Any, property: KProperty<*>): T = value ?: throw InjectNotCalledException()

        fun provide(kodi: Kodi, scope: Scope) {
            value = kodi.instance(scope, key)
        }
    }
}