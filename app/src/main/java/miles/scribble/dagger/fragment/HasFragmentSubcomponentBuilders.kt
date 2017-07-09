package miles.scribble.dagger.fragment;

import android.app.Activity
import android.support.v4.app.Fragment
import miles.scribble.dagger.activity.ActivityComponentBuilder

interface HasFragmentSubcomponentBuilders {

    fun getBuilder(fragmentClass: Class<out Fragment>) : FragmentComponentBuilder<*, *>
}