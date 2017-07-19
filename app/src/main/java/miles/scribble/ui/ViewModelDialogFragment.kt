package miles.scribble.ui

import android.arch.lifecycle.LifecycleRegistry
import android.arch.lifecycle.LifecycleRegistryOwner
import android.arch.lifecycle.ViewModel
import android.content.Context
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v4.app.Fragment
import miles.scribble.dagger.fragment.HasFragmentSubcomponentBuilders

/**
 * Created by mbpeele on 7/18/17.
 */
abstract class ViewModelDialogFragment<VM : ViewModel> : DialogFragment(), LifecycleRegistryOwner {

    private lateinit var lifecycleRegistry: LifecycleRegistry
    lateinit var viewModel: VM

    override fun onAttach(context: Context) {
        super.onAttach(context)

        viewModel = inject(context as HasFragmentSubcomponentBuilders)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleRegistry = LifecycleRegistry(this)
    }

    abstract fun inject(hasFragmentSubcomponentBuilders: HasFragmentSubcomponentBuilders): VM

    override fun getLifecycle(): LifecycleRegistry {
        return lifecycleRegistry
    }
}