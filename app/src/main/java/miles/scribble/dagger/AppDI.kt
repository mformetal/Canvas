package miles.scribble.dagger

import android.app.Application
import dagger.Component
import dagger.Module
import dagger.Provides
import miles.scribble.data.Datastore
import miles.scribble.home.drawing.DrawingCurve
import miles.scribble.ui.BaseFragment
import miles.scribble.ui.widget.CanvasLayout
import miles.scribble.ui.widget.CircleFabMenu
import javax.inject.Singleton

/**
 * Created by mbpeele on 6/25/17.
 */
@Module
@Singleton
class ApplicationModule(private val mApplication: Application) {

    @Provides
    @Singleton
    fun provideAppContext() : Application = mApplication

    @Provides
    @Singleton
    fun getDatastore(mApplication: Application): Datastore = Datastore(mApplication)
}

@Singleton
@Component(modules = arrayOf(ApplicationModule::class))
interface ApplicationComponent {

    fun inject(drawingCurve: DrawingCurve)

}