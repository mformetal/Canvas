package miles.scribble

import android.app.Application
import miles.kodi.Kodi
import miles.kodi.api.builder.bind
import miles.kodi.provider.component

/**
 * Created from milespeele on 7/3/15.
 */
class App : Application() {

    lateinit var kodi : Kodi

    override fun onCreate() {
        super.onCreate()

        kodi = Kodi.init {
            bind<Application>() using component(this@App)
        }
    }
}
