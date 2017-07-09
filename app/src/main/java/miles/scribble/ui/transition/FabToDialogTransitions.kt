package miles.scribble.ui.transition

import android.animation.*
import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.graphics.Point
import android.support.design.widget.CoordinatorLayout
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.transition.ArcMotion
import android.transition.ChangeBounds
import android.transition.TransitionValues
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import miles.scribble.R
import miles.scribble.ui.widget.CanvasLayout
import miles.scribble.ui.widget.RoundedFrameLayout
import miles.scribble.util.ViewUtils

/**
 * Created by mbpeele on 7/9/17.
 */
class TransitionFabToDialog(private val context: Context) : ChangeBounds() {

    override fun createAnimator(sceneRoot: ViewGroup, startValues: TransitionValues, endValues: TransitionValues): Animator {
        val startColor = Color.WHITE
        val endColor = ContextCompat.getColor(context, R.color.primary_dark)

        val views = targets
        val fab = views[0]
        val fabFrame = views[1] as RoundedFrameLayout
        val layout = views[2] as CanvasLayout

        val size = Point()
        (context as Activity).windowManager.defaultDisplay.getSize(size)
        val yDiff = (layout.height - size.y).toFloat()

        val xRatio = fab.width.toFloat() / fabFrame.width.toFloat()
        val yRatio = fab.height.toFloat() / fabFrame.height.toFloat()

        val translationX = fab.x - fab.width * .25f - (fabFrame.width / 2).toFloat()
        val translationY = fab.y + fab.height * 2.75f

        fabFrame.scaleX = xRatio
        fabFrame.scaleY = yRatio
        fabFrame.translationX = translationX
        fabFrame.translationY = translationY + yDiff
        fabFrame.visibility = View.VISIBLE

        val corner = ObjectAnimator.ofFloat<RoundedFrameLayout>(fabFrame,
                RoundedFrameLayout.CORNERS, fabFrame.width.toFloat(), 0f)
                .setDuration(350)
        corner.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationStart(animation: Animator) {
                fabFrame.isAnimating = true
            }

            override fun onAnimationEnd(animation: Animator) {
                fabFrame.isAnimating = false
            }
        })

        val alpha = ObjectAnimator.ofFloat(layout.getChildAt(3), View.ALPHA, .5f).setDuration(350)

        val background = ObjectAnimator.ofArgb(fabFrame,
                ViewUtils.BACKGROUND, startColor, endColor)
                .setDuration(350)

        val position = ObjectAnimator.ofPropertyValuesHolder(fabFrame,
                PropertyValuesHolder.ofFloat(View.TRANSLATION_X, 0f),
                PropertyValuesHolder.ofFloat(View.TRANSLATION_Y, 0f)).setDuration(350)

        val scale = ObjectAnimator.ofPropertyValuesHolder(fabFrame,
                PropertyValuesHolder.ofFloat(View.SCALE_X, fabFrame.scaleX, 1f),
                PropertyValuesHolder.ofFloat(View.SCALE_Y, fabFrame.scaleX, 1f))
                .setDuration(350)

        val animatorSet = AnimatorSet()
        animatorSet.playTogether(alpha, background, corner, position, scale)
        animatorSet.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationStart(animation: Animator) {
                fab.visibility = View.GONE
            }
        })

        return animatorSet
    }
}

class TransitionDialogToFab(private val context: Context) : ChangeBounds() {

    override fun createAnimator(sceneRoot: ViewGroup, startValues: TransitionValues, endValues: TransitionValues): Animator {
        super.createAnimator(sceneRoot, startValues, endValues)

        val startColor = ContextCompat.getColor(context, R.color.primary_dark)
        val endColor = Color.WHITE

        val views = targets
        val fab = views[0]
        val fabFrame = views[1] as RoundedFrameLayout
        val layout = views[2] as CanvasLayout

        val fabRadius = ViewUtils.radius(fab)
        val fabCenterX = fab.x + fabRadius
        val fabCenterY = fab.y + fabRadius
        val translationX = fabCenterX - (fabFrame.width / 2).toFloat() - fab.width * .75f
        val translationY = fabCenterY + fab.height * 3.5f

        val alpha = ObjectAnimator.ofFloat(layout.getChildAt(3), View.ALPHA, 1f).setDuration(350)

        val corner = ObjectAnimator.ofFloat<RoundedFrameLayout>(fabFrame,
                RoundedFrameLayout.CORNERS, 0f, fabFrame.width.toFloat())
                .setDuration(350)
        corner.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationStart(animation: Animator) {
                super.onAnimationStart(animation)
                fabFrame.isAnimating = true
            }

            override fun onAnimationEnd(animation: Animator) {
                super.onAnimationEnd(animation)
                fabFrame.isAnimating = false
            }
        })

        val background = ObjectAnimator.ofArgb(fabFrame,
                ViewUtils.BACKGROUND, startColor, endColor)
                .setDuration(350)

        val arcMotion = ArcMotion()
        arcMotion.minimumVerticalAngle = 70f
        arcMotion.minimumHorizontalAngle = 15f
        val motionPath = arcMotion.getPath(0f, 0f, translationX, translationY)
        val position = ObjectAnimator.ofFloat(fabFrame, View.TRANSLATION_X, View
                .TRANSLATION_Y, motionPath)
                .setDuration(350)

        val scalerX = PropertyValuesHolder.ofFloat(View.SCALE_X,
                fabFrame.scaleX, fab.width.toFloat() / fabFrame.width.toFloat())
        val scalerY = PropertyValuesHolder.ofFloat(View.SCALE_Y,
                fabFrame.scaleX, fab.height.toFloat() / fabFrame.height.toFloat())
        val scale = ObjectAnimator.ofPropertyValuesHolder(fabFrame, scalerX, scalerY)
                .setDuration(350)

        val animatorSet = AnimatorSet()
        animatorSet.playTogether(background, position, corner, alpha, scale)
        animatorSet.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                fabFrame.visibility = View.GONE
                fab.visibility = View.VISIBLE
            }
        })

        return animatorSet
    }
}

fun makeFabDialogTransitions(context: Context, view: View, fabFrame: FrameLayout, fragment: Fragment) {
    val transitionFabToDialog = TransitionFabToDialog(context)
    transitionFabToDialog.addTarget(view)
    transitionFabToDialog.addTarget(fabFrame)
    transitionFabToDialog.addTarget(fabFrame.parent as CoordinatorLayout)

    val transitionDialogToFab = TransitionDialogToFab(context)
    transitionDialogToFab.addTarget(view)
    transitionDialogToFab.addTarget(fabFrame)
    transitionDialogToFab.addTarget(fabFrame.parent as CoordinatorLayout)

    fragment.enterTransition = transitionFabToDialog
    fragment.returnTransition = transitionDialogToFab
    fragment.exitTransition = transitionDialogToFab
}