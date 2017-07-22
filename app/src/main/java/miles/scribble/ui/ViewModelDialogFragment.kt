package miles.scribble.ui

import android.app.Dialog
import android.arch.lifecycle.LifecycleRegistry
import android.arch.lifecycle.LifecycleRegistryOwner
import android.arch.lifecycle.ViewModel
import android.content.Context
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import miles.scribble.dagger.fragment.HasFragmentSubcomponentBuilders

/**
 * Created by mbpeele on 7/18/17.
 */
abstract class ViewModelDialogFragment<VM : ViewModel> : DialogFragment(), LifecycleRegistryOwner {

    private lateinit var lifecycleRegistry: LifecycleRegistry
    lateinit var viewModel: VM

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleRegistry = LifecycleRegistry(this)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        viewModel = inject(context as HasFragmentSubcomponentBuilders)

        return super.onCreateDialog(savedInstanceState)
    }

    abstract fun inject(hasFragmentSubcomponentBuilders: HasFragmentSubcomponentBuilders): VM

    override fun getLifecycle(): LifecycleRegistry {
        return lifecycleRegistry
    }
}