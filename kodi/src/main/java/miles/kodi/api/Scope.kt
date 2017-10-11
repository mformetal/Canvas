package miles.kodi.api

import kotlin.reflect.KClass

/**
 * Created by peelemil on 10/11/17.
 */
data class Scope(private val scopingClass: KClass<*>)

inline fun <reified T> scoped() = Scope(T::class)