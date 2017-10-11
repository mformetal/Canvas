package miles.kodi.api

/**
 * Created by peelemil on 10/11/17.
 */
interface ScopeBuilder {

    fun dependsOn(scope: Scope) : ScopeBuilder

    fun with(scope: Scope) : ScopeBuilder

    fun build(block: KodiBuilder.() -> Unit) : ScopeBuilder

}