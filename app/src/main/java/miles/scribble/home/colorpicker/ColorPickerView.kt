package miles.scribble.home.colorpicker

import android.animation.ObjectAnimator
import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.support.v4.view.animation.FastOutLinearInInterpolator
import android.support.v4.view.animation.FastOutSlowInInterpolator
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.View
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.SeekBar
import android.widget.TextView
import miles.scribble.R
import miles.scribble.util.extensions.inflater
import miles.scribble.util.android.FixedTextDrawable
import miles.scribble.util.extensions.lazyInflate

/**
 * Created from mbpeele on 7/8/17.
 */
class ColorPickerView : FrameLayout {

    private val currentColorView by lazyInflate<View>(R.id.current_color)

    private val redBar by lazyInflate<SeekBar>(R.id.red_bar)
    private val redInput by lazyInflate<TextView>(R.id.red_input)

    private val greenBar by lazyInflate<SeekBar>(R.id.green_bar)
    private val greenInput by lazyInflate<TextView>(R.id.green_input)

    private val blueBar by lazyInflate<SeekBar>(R.id.blue_bar)
    private val blueInput by lazyInflate<TextView>(R.id.blue_input)

    private val hexInput by lazyInflate<EditText>(R.id.hex_input)

    lateinit var viewModel : ColorPickerViewModel

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init()
    }

    private fun init() {
        val inflater = (context as Activity).inflater()
        inflater.inflate(R.layout.color_picker, this, true)
    }

    override fun onFinishInflate() {
        super.onFinishInflate()

        viewModel = ColorPickerViewModel { oldColor : Int, newColor: Int ->
            val colorViewAnimator = ObjectAnimator.ofArgb(oldColor, newColor)
            colorViewAnimator.duration = resources.getInteger(android.R.integer.config_shortAnimTime).toLong()
            colorViewAnimator.interpolator = FastOutSlowInInterpolator()
            colorViewAnimator.addUpdateListener {
                currentColorView.setBackgroundColor(it.animatedValue as Int)
            }
            colorViewAnimator.start()
        }

        hexInput.setCompoundDrawablesWithIntrinsicBounds(FixedTextDrawable("#", hexInput),
                null, null, null)
        hexInput.compoundDrawablePadding = context.resources.getDimension(R.dimen.padding_normal).toInt()
        hexInput.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(editable: Editable) {
                if (hexInput.hasFocus()) {
                    val oldColor = viewModel.currentColor

                    viewModel.parseIntFromString(editable.toString())

                    val newColor = viewModel.currentColor

                    if (oldColor != newColor) {
                        val seekBarAnimator = ObjectAnimator.ofArgb(oldColor, newColor)
                        seekBarAnimator.duration = resources.getInteger(android.R.integer.config_shortAnimTime).toLong()
                        seekBarAnimator.interpolator = FastOutSlowInInterpolator()
                        seekBarAnimator.addUpdateListener {
                            val color = it.animatedValue as Int
                            redBar.progress = Color.red(color)
                            greenBar.progress = Color.green(color)
                            blueBar.progress = Color.blue(color)
                        }
                        seekBarAnimator.start()

                        hexInput.setText(viewModel.hexString)
                        hexInput.setSelection(hexInput.text.length)
                    }
                }
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) { }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) { }
        })

        redBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, p1: Int, fromUser: Boolean) {
                if (fromUser) {
                    computeColor()
                }

                redInput.text = viewModel.redString
            }

            override fun onStartTrackingTouch(p0: SeekBar?) { }

            override fun onStopTrackingTouch(p0: SeekBar?) { }
        })

        greenBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, p1: Int, fromUser: Boolean) {
                if (fromUser) {
                    computeColor()
                }

                greenInput.text = viewModel.greenString
            }

            override fun onStartTrackingTouch(p0: SeekBar?) { }

            override fun onStopTrackingTouch(p0: SeekBar?) { }
        })

        blueBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, p1: Int, fromUser: Boolean) {
                if (fromUser) {
                    computeColor()
                }

                blueInput.text = viewModel.blueString
            }

            override fun onStartTrackingTouch(p0: SeekBar?) { }

            override fun onStopTrackingTouch(p0: SeekBar?) { }
        })
    }

    private fun computeColor() {
        val red = redBar.progress
        val green = greenBar.progress
        val blue = blueBar.progress
        viewModel.computeColor(red, green, blue)

        hexInput.setText(viewModel.hexString)
    }

    fun setColor(color: Int) {
        viewModel.currentColor = color

        hexInput.clearFocus()
        hexInput.setText(viewModel.hexString)

        redBar.progress = viewModel.red
        greenBar.progress = viewModel.green
        blueBar.progress = viewModel.blue
    }
}