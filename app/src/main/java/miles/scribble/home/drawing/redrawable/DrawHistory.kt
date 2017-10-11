package miles.scribble.home.drawing.redrawable

import android.graphics.Canvas
import java.util.*

/**
 * Created using mbpeele on 8/13/17.
 */
class DrawHistory(private val history: Stack<Redrawable> = Stack(),
                  private val redoHistory : Stack<Redrawable> = Stack()) {

    val canUndo : Boolean get() = history.isNotEmpty()
    val canRedo : Boolean get() = redoHistory.isNotEmpty()

    fun undo() {
        redoHistory.push(history.pop())
    }

    fun redo() {
        history.push(redoHistory.pop())
    }

    fun push(redrawable: Redrawable) {
        history.push(redrawable)
    }

    fun redraw(canvas: Canvas) {
        history.forEach { it.draw(canvas) }
    }
}