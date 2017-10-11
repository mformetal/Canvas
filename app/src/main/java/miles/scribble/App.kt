package miles.scribble

import android.app.Application
import miles.kodi.Kodi
import miles.kodi.api.HasKodi

/**
 * Created from milespeele on 7/3/15.
 */
class App : Application() {

    override val kodi by lazy {
        Kodi.init {  }
    }

    override fun onCreate() {
        super.onCreate()
    }
}
