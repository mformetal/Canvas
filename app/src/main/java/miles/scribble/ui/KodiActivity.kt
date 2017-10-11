package miles.scribble.ui

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import miles.kodi.Kodi
import miles.kodi.api.ScopeRegistry
import miles.scribble.util.extensions.app

/**
 * Created by mbpeele on 10/8/17.
 */
abstract class KodiActivity : AppCompatActivity() {

    var scopeRegistry: ScopeRegistry ?= null

    override fun onCreate(savedInstanceState: Bundle?) {
        installModule(app.kodi)
        super.onCreate(savedInstanceState)
    }

    override fun onDestroy() {
        super.onDestroy()

        scopeRegistry?.unregister()
        scopeRegistry = null
    }

    abstract fun installModule(kodi: Kodi) : ScopeRegistry
}