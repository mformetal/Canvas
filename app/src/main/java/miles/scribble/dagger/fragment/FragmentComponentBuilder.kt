package miles.scribble.dagger.fragment

import miles.scribble.dagger.activity.ActivityComponent
import miles.scribble.dagger.activity.ActivityComponentBuilder
import miles.scribble.dagger.activity.ActivityModule

interface FragmentComponentBuilder<M : FragmentModule<*>, C : FragmentComponent<*>> {

    fun module(activiyModule: M) : FragmentComponentBuilder<M, C>

    fun build() : C

}