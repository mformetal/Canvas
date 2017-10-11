package miles.scribble.ui.widget

import android.content.Context
import android.support.design.widget.CoordinatorLayout
import android.support.design.widget.Snackbar
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup

/**
 * Created using mbpeele on 9/4/15.
 */
class SnackbarBehavior(context: Context, attrs: AttributeSet) : CoordinatorLayout.Behavior<ViewGroup>() {

    override fun layoutDependsOn(parent: CoordinatorLayout, child: ViewGroup, dependency: View): Boolean {
        return dependency is Snackbar.SnackbarLayout
    }

    override fun onDependentViewChanged(parent: CoordinatorLayout, child: ViewGroup, dependency: View): Boolean {
        val translationY = Math.max(0f, dependency.height - dependency.translationY)
        child.translationY = -translationY
        return true
    }
}
