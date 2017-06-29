package miles.scribble.dagger.activity;

import android.app.Activity
import dagger.Module
import dagger.Provides

@Module
abstract class ActivityModule<out T : Activity>(val activity: T) {

    @Provides
    @ActivityScope
    fun activity() : T {
        return activity
    }
}