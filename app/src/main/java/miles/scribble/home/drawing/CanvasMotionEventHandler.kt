package miles.scribble.home.drawing

import android.graphics.Paint
import android.view.MotionEvent
import miles.scribble.home.drawing.redrawable.RedrawableLines
import miles.scribble.home.viewmodel.HomeState
import miles.scribble.util.extensions.copy

/**
 * Created by mbpeele on 8/12/17.
 */
class CanvasMotionEventHandler {

    fun handleTouchDown(motionEvent: MotionEvent, state: HomeState) : HomeState {
        val x = motionEvent.x
        val y = motionEvent.y
        val drawType = state.drawType

        return if (drawType is DrawType.Normal || drawType is DrawType.Ink) {
            state.stroke.addPoint(x, y, state.canvas, state.paint)
            state.copy(stroke = state.stroke.copy(),
                    activePointer = motionEvent.getPointerId(0),
                    lastX = x, lastY = y)
        } else if (drawType is DrawType.Ink) {
            val inkx = Math.round(x)
            val inky = Math.round(y - state.bitmap.height * .095f)
            if (0 <= inkx && inkx <= state.width - 1 &&
                    0 <= inky && inky <= state.height - 1) {
                state.copy(strokeColor = state.bitmap.getPixel(inkx, inky),
                        activePointer = motionEvent.getPointerId(0),
                        lastX = x, lastY = y)
            } else {
                state.copy(activePointer = motionEvent.getPointerId(0),
                        lastX = x, lastY = y)
            }
        } else {
            state
        }
    }

    fun handlePointerDown(state: HomeState) = state

    fun handleTouchMove(motionEvent: MotionEvent, state: HomeState) : HomeState {
        val pointerIndex = motionEvent.findPointerIndex(state.activePointer)
        val x = motionEvent.getX(pointerIndex)
        val y = motionEvent.getY(pointerIndex)
        val drawType = state.drawType

        return if (drawType is DrawType.Normal || drawType is DrawType.Erase) {
            val stroke = state.stroke.apply {
                for (i in 0 until motionEvent.historySize) {
                    addPoint(
                            motionEvent.getHistoricalX(pointerIndex, i),
                            motionEvent.getHistoricalY(pointerIndex, i),
                            state.canvas, state.paint)
                }
                addPoint(x, y, state.canvas, state.paint)
            }

            state.copy(stroke = stroke.copy(), lastX = x, lastY = y)
        } else if (drawType is DrawType.Ink) {
            val inkx = Math.round(x)
            val inky = Math.round(y - state.bitmap.height * .095f)
            if (0 <= inkx && inkx <= state.width - 1 &&
                    0 <= inky && inky <= state.height - 1) {
                state.copy(strokeColor = state.bitmap.getPixel(inkx, inky),
                        lastX = x, lastY = y)
            } else {
                state.copy(lastX = x, lastY = y)
            }
        } else {
            state
        }
    }

    fun handleTouchUp(motionEvent: MotionEvent, state: HomeState) : HomeState {
        val drawType = state.drawType

        return if (drawType is DrawType.Normal || drawType is DrawType.Erase) {
            val history = state.history.copy().apply {
                push(RedrawableLines(state.stroke.points, state.paint))
            }
            state.copy(history = history, stroke = Stroke(),
                    lastX = motionEvent.x, lastY = motionEvent.y)
        } else if (drawType is DrawType.Ink) {
            state.copy(paint = Paint(state.paint).apply {
                color = state.strokeColor
            }, drawType = DrawType.Normal(), lastX = motionEvent.x, lastY = motionEvent.y)
        } else {
            state
        }
    }

    fun handlePointerUp(motionEvent: MotionEvent, state: HomeState) : HomeState {
        val pointerIndex = motionEvent.action and MotionEvent.ACTION_POINTER_INDEX_MASK shr MotionEvent.ACTION_POINTER_INDEX_SHIFT
        val pointerId = motionEvent.getPointerId(pointerIndex)

        return if (pointerId == state.activePointer) {
            val newPointerIndex = if (pointerIndex == 0) 1 else 0
            val lastX = motionEvent.getX(newPointerIndex)
            val lastY = motionEvent.getY(newPointerIndex)
            val activePointer = motionEvent.getPointerId(newPointerIndex)
            state.copy(lastY = lastY, lastX = lastX, activePointer = activePointer)
        } else {
            state
        }
    }

    fun handleMotionCancel(state: HomeState) : HomeState {
        return state.copy(activePointer = HomeState.INVALID_POINTER)
    }
}