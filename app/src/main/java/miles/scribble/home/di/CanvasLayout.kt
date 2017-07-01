package miles.scribble.home.di

import dagger.MembersInjector
import dagger.Module
import dagger.Subcomponent
import miles.scribble.dagger.ViewScope
import miles.scribble.ui.widget.CanvasLayout

/**
 * Created by mbpeele on 6/30/17.
 */
@ViewScope
@Subcomponent(modules = arrayOf(CanvasLayoutModule::class))
interface CanvasLayoutComponent : MembersInjector<CanvasLayout>

@Module
class CanvasLayoutModule