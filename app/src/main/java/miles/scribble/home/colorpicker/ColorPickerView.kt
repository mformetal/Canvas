package miles.scribble.home.colorpicker

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.View
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.SeekBar
import butterknife.BindView
import butterknife.ButterKnife
import miles.scribble.R
import miles.scribble.util.extensions.inflater

/**
 * Created by mbpeele on 7/8/17.
 */
class ColorPickerView : FrameLayout {

    @BindView(R.id.current_color)
    lateinit var currentColorView : View
    @BindView(R.id.red_bar)
    lateinit var redBar : SeekBar
    @BindView(R.id.green_bar)
    lateinit var greenBar : SeekBar
    @BindView(R.id.blue_bar)
    lateinit var blueBar : SeekBar
    @BindView(R.id.hex_input)
    lateinit var hexInput : EditText

    var currentColor : Int = Color.BLACK
        private set(value) {
            currentColorView.setBackgroundColor(value)
        }

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

        hexInput.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable) {
                try {
                    currentColor = Color.parseColor(p0.toString())
                } catch (e: IllegalArgumentException) { }
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) { }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) { }
        })

        redBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                computeColor()
            }

            override fun onStartTrackingTouch(p0: SeekBar?) { }

            override fun onStopTrackingTouch(p0: SeekBar?) { }
        })

        greenBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                computeColor()
            }

            override fun onStartTrackingTouch(p0: SeekBar?) { }

            override fun onStopTrackingTouch(p0: SeekBar?) { }
        })

        blueBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                computeColor()
            }

            override fun onStartTrackingTouch(p0: SeekBar?) { }

            override fun onStopTrackingTouch(p0: SeekBar?) { }
        })
    }

    private fun computeColor() {
        val red = redBar.progress
        val green = greenBar.progress
        val blue = blueBar.progress
        currentColor = Color.argb(1, red, green, blue)
    }

    fun setColor(color: Int) {
        val red = Color.red(color)
        val green = Color.green(color)
        val blue = Color.blue(color)
        redBar.progress = red
        greenBar.progress = green
        blueBar.progress = blue
        currentColor = Color.argb(1, red, green, blue)
    }


}