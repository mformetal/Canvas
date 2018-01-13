package miles.scribble

import android.app.Application
import mformetal.kodi.android.KodiApp
import mformetal.kodi.core.Kodi
import mformetal.kodi.core.api.builder.bind
import mformetal.kodi.core.provider.component
import miles.scribble.home.viewmodel.HomeViewModelFactory

/**
 * Created from milespeele on 7/3/15.
 */
class App : KodiApp() {

    override fun createRootKodi(): Kodi {
        return Kodi.init {
            val app = this@App

            bind<Application>() using component(app)
            bind<HomeViewModelFactory>() using component(HomeViewModelFactory(app))
        }
    }
}
