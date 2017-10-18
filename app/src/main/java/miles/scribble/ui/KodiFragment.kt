package miles.scribble.ui

import android.os.Bundle
import android.support.v4.app.Fragment
import miles.kodi.Kodi
import miles.kodi.api.ScopeRegistry
import miles.kodi.api.injection.InjectionRegistry
import miles.kodi.api.injection.KodiInjector
import miles.scribble.util.extensions.kodi

abstract class KodiFragment : Fragment() {

    val injector : InjectionRegistry = KodiInjector()
    lateinit var scopeRegistry: ScopeRegistry

    override fun onCreate(savedInstanceState: Bundle?) {
        scopeRegistry = installModule(activity.kodi)
        injector.inject(activity.kodi, scopeRegistry.scope)
        super.onCreate(savedInstanceState)
    }

    override fun onDestroy() {
        super.onDestroy()

        scopeRegistry.unregister()
    }

    abstract fun installModule(kodi: Kodi) : ScopeRegistry

}