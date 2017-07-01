package miles.scribble

import android.app.Activity
import android.app.Application
import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import android.content.Context
import dagger.Component
import dagger.Module
import dagger.Provides

import miles.scribble.dagger.activity.ActivityBindingModule
import miles.scribble.dagger.activity.ActivityComponentBuilder
import miles.scribble.dagger.activity.HasActivitySubcomponentBuilders
import miles.scribble.dagger.viewmodel.CanvasViewModelFactory
import miles.scribble.dagger.viewmodel.ViewModelModule
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

/**
 * Created by milespeele on 7/3/15.
 */
class MainApp : Application(), HasActivitySubcomponentBuilders {

    @Inject
    lateinit var activityComponentBuilders: Map<Class<out Activity>, @JvmSuppressWildcards Provider<ActivityComponentBuilder<*, *>>>

    lateinit var applicationComponent: AppComponent
        private set

    override fun onCreate() {
        super.onCreate()

        applicationComponent = DaggerAppComponent.builder()
                .appModule(AppModule(this))
                .build()
        applicationComponent.inject(this)
    }

    override fun getBuilder(activityClass: Class<out Activity>): ActivityComponentBuilder<*, *> {
        return activityComponentBuilders[activityClass]!!.get()
    }
}

@Module(includes = arrayOf(ActivityBindingModule::class, ViewModelModule::class))
@Singleton
class AppModule(private val application: Application) {

    @Provides
    @Singleton
    fun appContext() : Context = application

    @Provides
    @Singleton
    fun factory(creators: Map<Class<out ViewModel>, @JvmSuppressWildcards Provider<ViewModel>>)
            : ViewModelProvider.Factory {
        return CanvasViewModelFactory(creators)
    }
}

@Singleton
@Component(modules = arrayOf(AppModule::class))
interface AppComponent {

    fun inject(app: MainApp)

}
