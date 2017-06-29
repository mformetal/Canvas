package miles.scribble.dagger.activity;

import android.app.Activity

interface HasActivitySubcomponentBuilders {

    fun getBuilder(activityClass: Class<out Activity>) : ActivityComponentBuilder<*, *>
}