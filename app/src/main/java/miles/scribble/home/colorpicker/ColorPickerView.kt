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
import butterknife.BindView
import butterknife.ButterKnife
import miles.scribble.R
import miles.scribble.util.extensions.inflater
import miles.scribble.util.android.FixedTextDrawable

/**
 * Created by mbpeele on 7/8/17.
 */
class ColorPickerView : FrameLayout {

    @BindView(R.id.current_color)
    lateinit var currentColorView : View
    @BindView(R.id.red_bar)
    lateinit var redBar : SeekBar
    @BindView(R.id.red_input)
    lateinit var redInput : TextView

    @BindView(R.id.green_bar)
    lateinit var greenBar : SeekBar
    @BindView(R.id.green_input)
    lateinit var greenInput : TextView

    @BindView(R.id.blue_bar)
    lateinit var blueBar : SeekBar
    @BindView(R.id.blue_input)
    lateinit var blueInput : TextView

    @BindView(R.id.hex_input)
    lateinit var hexInput : EditText

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
        ButterKnife.bind(this)

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
        redInput.text = viewModel.redString

        greenBar.progress = viewModel.green
        greenInput.text = viewModel.greenString

        blueBar.progress = viewModel.blue
        blueInput.text = viewModel.blueString
    }
}