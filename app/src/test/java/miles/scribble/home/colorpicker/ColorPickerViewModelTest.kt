package miles.scribble.home.colorpicker

import assertk.assert
import assertk.assertions.isEqualTo
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.verifyZeroInteractions
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

/**
 * Created using mbpeele on 8/12/17.
 */
@RunWith(MockitoJUnitRunner::class)
class ColorPickerViewModelTest {

    lateinit var viewModel : ColorPickerViewModel
    val colorChangeListener = mock<(Int, Int) -> Unit>()

    @Before
    fun setup() {
        viewModel = ColorPickerViewModel(colorChangeListener = colorChangeListener)
    }

    @Test
    fun testChangeListenerNotCalledForSameColor() {
        viewModel.currentColor = viewModel.currentColor
        verifyZeroInteractions(colorChangeListener)
    }

    @Test
    fun testChangeViewModelColorCallsListener() {
        val oldColor = viewModel.currentColor
        val newColor = viewModel.currentColor + 1
        viewModel.currentColor = newColor
        verify(colorChangeListener).invoke(oldColor, newColor)
    }

    @Test
    fun testHexStringFromCurrentColor() {
        viewModel.currentColor = "000000".toLong(16).toInt()
        assert("#" + viewModel.hexString).isEqualTo("#000000")

        viewModel.currentColor = "FFFFFF".toLong(16).toInt()
        assert("#" + viewModel.hexString).isEqualTo("#FFFFFF")

        viewModel.currentColor = "FF0000".toLong(16).toInt()
        assert("#" + viewModel.hexString).isEqualTo("#FF0000")
    }

    @Test
    fun testOnlyFullHexStringParsed() {
        val oldColor = viewModel.currentColor
        (0 until 5)
                .map { "0".repeat(it) }
                .forEach { assert(viewModel.currentColor).isEqualTo(oldColor) }
    }
}