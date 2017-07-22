package miles.scribble.home.colorpicker

import android.graphics.Color

/**
 * Created by mbpeele on 7/18/17.
 */
class ColorPickerViewModel(currentColor : Int = Color.BLACK, private val colorChangeListener: (Int, Int) -> Unit) {

    var currentColor : Int = currentColor
        set(value) {
            if (field != value) {
                colorChangeListener.invoke(field, value)
                field = value
            }
        }

    val hexString: String
        get() = String.format("#%06X", 0xFFFFFF and currentColor).replace("#", "")

    val red : Int
        get() = Color.red(currentColor)
    val green: Int
        get() = Color.green(currentColor)
    val blue: Int
        get() = Color.blue(currentColor)

    val redString : String
        get() = padStringToThreeCharacters(red.toString())
    val greenString : String
        get() = padStringToThreeCharacters(green.toString())
    val blueString : String
        get() = padStringToThreeCharacters(blue.toString())

    fun parseIntFromString(string: String) {
        if (string.length == 6) {
            currentColor = Color.parseColor("#" + string)
        }
    }

    fun padStringToThreeCharacters(string: String) : String {
        return if (string.length == 1) {
            " $string "
        } else if (string.length == 2) {
            " $string"
        } else {
            string
        }
    }

    fun computeColor(red: Int, green: Int, blue: Int) {
        currentColor = Color.argb(255, red, green, blue)
    }
}