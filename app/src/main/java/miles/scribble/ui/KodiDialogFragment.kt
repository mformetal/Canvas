package miles.scribble.ui

import android.os.Bundle
import android.support.v4.app.DialogFragment
import miles.kodi.Kodi
import miles.kodi.api.ScopeRegistry
import miles.kodi.api.injection.InjectionRegistry
import miles.kodi.api.injection.KodiInjector
import miles.scribble.util.extensions.app
import miles.scribble.util.extensions.kodi

/**
 * Created using mbpeele on 10/8/17.
 */
abstract class KodiDialogFragment : DialogFragment() {

    val injector : InjectionRegistry = KodiInjector()
    lateinit var scopeRegistry: ScopeRegistry

    override fun onCreate(savedInstanceState: Bundle?) {
        scopeRegistry = installModule(app.kodi)
        injector.inject(app.kodi, scopeRegistry.scope)
        super.onCreate(savedInstanceState)
    }

    override fun onDestroy() {
        super.onDestroy()

        scopeRegistry.unregister()
    }

    abstract fun installModule(kodi: Kodi) : ScopeRegistry

}