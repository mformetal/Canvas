package miles.scribble.ui

import android.arch.lifecycle.ViewModel
import android.support.v4.app.DialogFragment

/**
 * Created by mbpeele on 7/18/17.
 */
abstract class ViewModelDialogFragment<VM : ViewModel> : DialogFragment() {

    lateinit var viewModel: VM

    abstract fun inject(): VM

}