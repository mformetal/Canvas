package miles.kodi.api.injection

import miles.kodi.Kodi
import miles.kodi.api.Scope
import miles.kodi.api.scoped
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KClass
import kotlin.reflect.KProperty

/**
 * Created by peelemil on 10/11/17.
 */
interface InjectionRegistry {

    fun <T : Any> register(type: KClass<T>, tag: String = ""): ReadOnlyProperty<Any, T>

    fun inject(kodi: Kodi)
}

inline fun <reified T : Any> InjectionRegistry.register(tag: String = "") = register(T::class, tag)