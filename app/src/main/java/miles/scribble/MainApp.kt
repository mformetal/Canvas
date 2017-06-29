package miles.scribble

import android.app.Activity
import android.app.Application
import android.os.Bundle
import dagger.Component
import dagger.Module
import dagger.Provides

import io.realm.Realm
import io.realm.RealmConfiguration
import miles.scribble.dagger.activity.ActivityBindingModule
import miles.scribble.dagger.activity.ActivityComponentBuilder
import miles.scribble.dagger.activity.HasActivitySubcomponentBuilders
import miles.scribble.home.drawing.DrawingCurve
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

/**
 * Created by milespeele on 7/3/15.
 */
class MainApp : Application(), HasActivitySubcomponentBuilders {

    @Inject
    lateinit var activityComponentBuilders: Map<Class<out Activity>, @JvmSuppressWildcards Provider<ActivityComponentBuilder<*, *>>>

    lateinit var applicationComponent: ApplicationComponent
        private set

    override fun onCreate() {
        super.onCreate()

        Realm.init(this)

        val realmConfiguration = RealmConfiguration.Builder()
                .build()

        Realm.setDefaultConfiguration(realmConfiguration)

        applicationComponent = DaggerApplicationComponent.create()
        applicationComponent.inject(this)
    }

    override fun getBuilder(activityClass: Class<out Activity>): ActivityComponentBuilder<*, *> {
        return activityComponentBuilders[activityClass]!!.get()
    }
}

@Module(includes = arrayOf(ActivityBindingModule::class))
@Singleton
class ApplicationModule(private val mApplication: Application) {

    @Provides
    @Singleton
    fun provideAppContext() : Application = mApplication
}

@Singleton
@Component(modules = arrayOf(ApplicationModule::class))
interface ApplicationComponent {

    fun inject(app: MainApp)

}
