package miles.scribble.dagger.activity;

interface ActivityComponentBuilder<M : ActivityModule<*>, C : ActivityComponent<*>> {

    fun module(activiyModule: M) : ActivityComponentBuilder<M, C>

    fun build() : C

}