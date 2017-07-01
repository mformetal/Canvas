package miles.scribble.home.di

import dagger.MembersInjector
import dagger.Module
import dagger.Provides
import dagger.Subcomponent
import miles.scribble.dagger.ViewScope
import miles.scribble.ui.widget.CanvasSurface

/**
 * Created by mbpeele on 6/30/17.
 */
@ViewScope
@Subcomponent(modules = arrayOf(CanvasSurfaceModule::class))
interface CanvasSurfaceComponent : MembersInjector<CanvasSurface>

@Module
class CanvasSurfaceModule