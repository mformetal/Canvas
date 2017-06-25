package miles.scribble.dagger

import android.app.Application
import dagger.Component
import dagger.Module
import dagger.Provides
import miles.scribble.data.Datastore
import miles.scribble.ui.activity.BaseActivity
import miles.scribble.ui.drawing.DrawingCurve
import miles.scribble.ui.fragment.BaseFragment
import miles.scribble.ui.widget.CanvasLayout
import miles.scribble.ui.widget.CircleFabMenu
import org.greenrobot.eventbus.EventBus
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

    val eventBus: EventBus
        @Provides
        @Singleton
        get() = EventBus()
}

@Singleton
@Component(modules = arrayOf(ApplicationModule::class))
interface ApplicationComponent {

    fun inject(drawingCurve: DrawingCurve)

    fun inject(baseActivity: BaseActivity)

    fun inject(viewFabMenu: CircleFabMenu)

    fun inject(canvasLayout: CanvasLayout)

    fun inject(fragment: BaseFragment)
}