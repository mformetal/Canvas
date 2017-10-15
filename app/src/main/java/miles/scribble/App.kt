package miles.scribble

import android.app.Application
import miles.kodi.Kodi
import miles.kodi.api.builder.bind
import miles.kodi.provider.component
import miles.kodi.provider.singleton
import miles.scribble.home.viewmodel.HomeViewModel
import miles.scribble.home.viewmodel.HomeViewModelFactory

/**
 * Created from milespeele on 7/3/15.
 */
class App : Application() {

    lateinit var kodi : Kodi

    override fun onCreate() {
        super.onCreate()

        kodi = Kodi.init {
            val app = this@App

            bind<Application>() using component(app)
            bind<HomeViewModel>() using singleton {
                HomeViewModelFactory(app).create(HomeViewModel::class.java)
            }
        }
    }
}
