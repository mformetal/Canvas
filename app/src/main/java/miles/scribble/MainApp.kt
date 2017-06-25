package miles.scribble

import android.app.Application

import io.realm.Realm
import io.realm.RealmConfiguration
import miles.scribble.dagger.ApplicationComponent
import miles.scribble.dagger.ApplicationModule
import miles.scribble.dagger.DaggerApplicationComponent

/**
 * Created by milespeele on 7/3/15.
 */
class MainApp : Application() {

    lateinit var applicationComponent: ApplicationComponent
        private set

    override fun onCreate() {
        super.onCreate()

        Realm.init(this)

        val realmConfiguration = RealmConfiguration.Builder()
                .build()

        Realm.setDefaultConfiguration(realmConfiguration)

        applicationComponent = DaggerApplicationComponent.builder()
                .applicationModule(ApplicationModule(this))
                .build()
    }
}
