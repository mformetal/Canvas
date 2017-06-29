package miles.scribble.home.di

import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import dagger.Module
import dagger.Provides
import io.realm.Realm
import miles.scribble.dagger.activity.ActivityModule
import miles.scribble.dagger.activity.ActivityScope
import miles.scribble.home.HomeActivity
import miles.scribble.home.viewmodel.HomeViewModel

/**
 * Created by mbpeele on 6/28/17.
 */
@Module
class HomeModule(activity: HomeActivity) : ActivityModule<HomeActivity>(activity) {

    val realm = Realm.getDefaultInstance()

    @Provides
    @ActivityScope
    fun provideRealm() = realm

    @Provides
    @ActivityScope
    fun viewModel() = ViewModelProviders.of(activity)[HomeViewModel::class.java]
}