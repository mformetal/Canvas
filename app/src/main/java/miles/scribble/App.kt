package miles.scribble

import android.app.Application
import android.content.Context
import miles.kodi.Kodi
import miles.kodi.api.HasKodi
import miles.kodi.kodi
import miles.kodi.module.singleton

/**
 * Created by milespeele on 7/3/15.
 */
class App : Application(), HasKodi {

    override val kodi: Kodi
        get() = kodi {
            root {
                bind<Context>() from singleton { this@App }
                bind<App>() from singleton { this@App }
            }
        }
}
