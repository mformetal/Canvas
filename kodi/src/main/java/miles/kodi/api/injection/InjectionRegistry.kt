package miles.kodi.api.injection

import miles.kodi.Kodi
import miles.kodi.api.Scope
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KClass

/**
 * Created by peelemil on 10/11/17.
 */
interface InjectionRegistry {

    fun <T : Any> register(type: KClass<T>, tag: String = ""): ReadOnlyProperty<Any, T>

    fun inject(kodi: Kodi, scope: Scope)
}

inline fun <reified T : Any> InjectionRegistry.register(tag: String = "") = register(T::class, tag)