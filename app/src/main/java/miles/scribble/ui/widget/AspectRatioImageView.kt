package miles.scribble.ui.widget

import android.content.Context
import android.support.v7.widget.AppCompatImageView
import android.util.AttributeSet
import miles.scribble.R

/**
 * Created using mbpeele on 1/11/16.
 */
class AspectRatioImageView : AppCompatImageView {

    private var aspectRatio: Float = DEFAULT_ASPECT_RATIO
        set(value) {
            if (aspectRatioEnabled) {
                field = value
                requestLayout()
            }
        }

    private var aspectRatioEnabled: Boolean = false
        set(value) {
            field = value
            requestLayout()
        }

    private var dominantMeasurement: Int = 0
        set(value) {
            if (dominantMeasurement != MEASUREMENT_HEIGHT && dominantMeasurement != MEASUREMENT_WIDTH) {
                throw IllegalArgumentException("Invalid measurement type.")
            }
            field = value
            requestLayout()
        }

    constructor(context: Context) : super(context) {
        init(null)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(attrs)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(attrs)
    }

    private fun init(attributeSet: AttributeSet?) {
        if (attributeSet != null) {
            val a = context.obtainStyledAttributes(attributeSet, R.styleable.AspectRatioImageView)
            aspectRatioEnabled = a.getBoolean(R.styleable.AspectRatioImageView_aspectRatioEnabled,
                    DEFAULT_ASPECT_RATIO_ENABLED)
            aspectRatio = a.getFloat(R.styleable.AspectRatioImageView_aspectRatio, DEFAULT_ASPECT_RATIO)
            dominantMeasurement = a.getInt(R.styleable.AspectRatioImageView_dominantMeasurement,
                    DEFAULT_DOMINANT_MEASUREMENT)
            a.recycle()
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        if (!aspectRatioEnabled) return

        val newWidth: Int
        val newHeight: Int
        when (dominantMeasurement) {
            MEASUREMENT_WIDTH -> {
                newWidth = measuredWidth
                newHeight = (newWidth * aspectRatio).toInt()
            }

            MEASUREMENT_HEIGHT -> {
                newHeight = measuredHeight
                newWidth = (newHeight * aspectRatio).toInt()
            }

            else -> throw IllegalStateException("Unknown measurement with ID " + dominantMeasurement)
        }

        setMeasuredDimension((newWidth / aspectRatio).toInt(), (newHeight / aspectRatio).toInt())
    }

    companion object {

        val MEASUREMENT_WIDTH = 0
        val MEASUREMENT_HEIGHT = 1

        private val DEFAULT_ASPECT_RATIO = 1f
        private val DEFAULT_ASPECT_RATIO_ENABLED = false
        private val DEFAULT_DOMINANT_MEASUREMENT = MEASUREMENT_WIDTH
    }
}
