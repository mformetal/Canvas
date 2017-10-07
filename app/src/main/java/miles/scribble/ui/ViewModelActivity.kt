package miles.scribble.ui

import android.arch.lifecycle.ViewModel
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import miles.scribble.App
import miles.scribble.util.extensions.app

/**
 * Created by milespeele on 7/14/15.
 */
abstract class ViewModelActivity<VM : ViewModel> : AppCompatActivity() {

    lateinit var viewModel : VM

    override fun onCreate(savedInstanceState: Bundle?) {
        viewModel = inject(app())

        super.onCreate(savedInstanceState)
    }

    abstract fun inject(app: App) : VM
}
