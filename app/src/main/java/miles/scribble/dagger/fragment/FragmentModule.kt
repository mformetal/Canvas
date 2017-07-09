package miles.scribble.dagger.fragment;

import android.support.v4.app.Fragment
import dagger.Module
import dagger.Provides
import miles.scribble.dagger.activity.ActivityScope

@Module
abstract class FragmentModule<out T : Fragment>(val fragment: T) {

    @Provides
    @ActivityScope
    fun fragment() : T {
        return fragment
    }
}