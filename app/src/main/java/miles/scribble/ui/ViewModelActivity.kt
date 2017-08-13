package miles.scribble.ui

import android.arch.lifecycle.LifecycleRegistry
import android.arch.lifecycle.LifecycleRegistryOwner
import android.arch.lifecycle.ViewModel
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import miles.scribble.util.extensions.app
import miles.scribble.App

/**
 * Created by milespeele on 7/14/15.
 */
abstract class ViewModelActivity<VM : ViewModel> : AppCompatActivity(), LifecycleRegistryOwner {

    private lateinit var lifecycleRegistry : LifecycleRegistry
    lateinit var viewModel : VM

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleRegistry = LifecycleRegistry(this)

        viewModel = inject(app())
    }

    abstract fun inject(app: App) : VM

    override fun getLifecycle(): LifecycleRegistry {
        return lifecycleRegistry
    }
}
