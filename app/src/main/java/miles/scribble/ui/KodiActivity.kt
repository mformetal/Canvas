package miles.scribble.ui

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import miles.kodi.Kodi
import miles.kodi.api.Scope
import miles.kodi.api.ScopeRegistry
import miles.kodi.api.injection.InjectionRegistry
import miles.kodi.api.injection.KodiInjector
import miles.scribble.util.extensions.app
import miles.scribble.util.extensions.kodi

/**
 * Created using mbpeele on 10/8/17.
 */
abstract class KodiActivity : AppCompatActivity() {

    val injector : InjectionRegistry = KodiInjector()
    lateinit var scopeRegistry: ScopeRegistry

    override fun onCreate(savedInstanceState: Bundle?) {
        scopeRegistry = installModule(kodi)
        injector.inject(kodi, scopeRegistry.scope)
        super.onCreate(savedInstanceState)
    }

    override fun onDestroy() {
        super.onDestroy()

        scopeRegistry.unregister()
    }

    abstract fun installModule(kodi: Kodi) : ScopeRegistry
}