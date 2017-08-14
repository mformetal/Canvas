package miles.scribble.home.drawing

import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.PointF
import android.view.MotionEvent
import miles.scribble.home.drawing.redrawable.RedrawableLines
import miles.scribble.home.viewmodel.HomeState
import miles.scribble.util.extensions.angle
import miles.scribble.util.extensions.copy
import miles.scribble.util.extensions.distance
import android.support.v4.widget.ViewDragHelper.INVALID_POINTER
import miles.scribble.home.viewmodel.PhotoMode

/**
 * Created by mbpeele on 8/12/17.
 */
class CanvasMotionEventHandler {

    fun handleTouchDown(motionEvent: MotionEvent, state: HomeState) : HomeState {
        val x = motionEvent.x
        val y = motionEvent.y

        return when (state.drawType) {
            DrawType.NORMAL, DrawType.ERASE -> {
                state.stroke.addPoint(x, y, state.canvas, state.paint)
                state.copy(stroke = state.stroke.copy(),
                        activePointer = motionEvent.getPointerId(0),
                        lastX = x, lastY = y)
            }
            DrawType.INK -> {
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
            }
            DrawType.PICTURE -> {
                val photoBitmapState = state.photoState.copy(
                        savedMatrix = Matrix(state.photoState.matrix),
                        startPoint = PointF(x, y),
                        photoMode = PhotoMode.DRAG)
                state.copy(photoState = photoBitmapState,
                        activePointer = motionEvent.getPointerId(0),
                        lastX = x, lastY = y)
            }
        }
    }

    fun handlePointerDown(motionEvent: MotionEvent, state: HomeState) : HomeState {
        return if (state.drawType == DrawType.PICTURE && motionEvent.pointerCount <= 2) {
            val distance = motionEvent.distance()
            val angle = motionEvent.angle()

            var savedMatrix = state.photoState.savedMatrix
            var midPoint = state.photoState.midPoint
            var photoMode = state.photoState.photoMode

            if (distance > 10f) {
                savedMatrix = Matrix(state.photoState.matrix)

                val horizontalDistance = motionEvent.getX(0) + motionEvent.getX(1)
                val verticalDistance = motionEvent.getY(0) + motionEvent.getY(1)
                midPoint = PointF(horizontalDistance / 2f, verticalDistance / 2f)
                photoMode = PhotoMode.ZOOM
            }

            val photoBitmapState = state.photoState.copy(oldDistance = distance, lastRotation = angle,
                    midPoint = midPoint, savedMatrix = savedMatrix, photoMode = photoMode)
            state.copy(photoState = photoBitmapState)
        } else {
            state
        }
    }

    fun handleTouchMove(motionEvent: MotionEvent, state: HomeState) : HomeState {
        val pointerIndex = motionEvent.findPointerIndex(state.activePointer)
        val x = motionEvent.getX(pointerIndex)
        val y = motionEvent.getY(pointerIndex)

        return when (state.drawType) {
            DrawType.NORMAL, DrawType.ERASE -> {
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
            }
            DrawType.INK -> {
                val inkx = Math.round(x)
                val inky = Math.round(y - state.bitmap.height * .095f)
                if (0 <= inkx && inkx <= state.width - 1 &&
                        0 <= inky && inky <= state.height - 1) {
                    state.copy(strokeColor = state.bitmap.getPixel(inkx, inky),
                            lastX = x, lastY = y)
                } else {
                    state.copy(lastX = x, lastY = y)
                }
            }
            DrawType.PICTURE -> {
                val photoBitmapState = state.photoState
                when (state.photoState.photoMode) {
                    PhotoMode.NONE -> state
                    PhotoMode.DRAG -> {
                        val matrix = Matrix(photoBitmapState.savedMatrix)
                        matrix.postTranslate(x - photoBitmapState.startPoint.x, y - photoBitmapState.startPoint.y)
                        state.copy(photoState = photoBitmapState.copy(matrix = matrix), lastX = x, lastY = y)
                    }
                    PhotoMode.ZOOM -> {
                        if (motionEvent.pointerCount == 2) {
                            val distance = motionEvent.distance()
                            val rotation = motionEvent.angle()

                            val matrix = Matrix(photoBitmapState.savedMatrix)

                            if (distance > 10f) {
                                val scale = distance / photoBitmapState.oldDistance
                                matrix.postScale(scale, scale,
                                        photoBitmapState.midPoint.x,
                                        photoBitmapState.midPoint.y)
                            }

                            matrix.postRotate(rotation - photoBitmapState.lastRotation,
                                    photoBitmapState.midPoint.x, photoBitmapState.midPoint.y)
                            state.copy(photoState = photoBitmapState.copy(matrix = matrix),
                                    lastX = x, lastY = y)
                        } else {
                            state.copy(lastX = x, lastY = y)
                        }
                    }
                }
            }
        }
    }

    fun handleTouchUp(motionEvent: MotionEvent, state: HomeState) : HomeState {
        return when (state.drawType) {
            DrawType.NORMAL, DrawType.ERASE -> {
                val redrawable = RedrawableLines(state.stroke.points, state.paint)
                state.history.push(redrawable)
                state.copy(stroke = Stroke(),
                        lastX = motionEvent.x, lastY = motionEvent.y)
            }
            DrawType.INK -> {
                state.copy(paint = Paint(state.paint).apply {
                    color = state.strokeColor
                }, drawType = DrawType.NORMAL, lastX = motionEvent.x, lastY = motionEvent.y)
            }
            DrawType.PICTURE -> {
                state.canvas.save()
                state.canvas.concat(state.photoState.matrix)
                state.canvas.drawBitmap(state.photoState.photoBitmap!!, 0f, 0f, null)
                state.canvas.restore()

                state.photoState.photoBitmap?.recycle()

                state.copy(drawType = DrawType.NORMAL,
                        photoState = state.photoState.copy(photoBitmap = null))
            }
        }
    }

    fun handlePointerUp(motionEvent: MotionEvent, state: HomeState) : HomeState {
        val pointerIndex = motionEvent.action and MotionEvent.ACTION_POINTER_INDEX_MASK shr MotionEvent.ACTION_POINTER_INDEX_SHIFT
        val pointerId = motionEvent.getPointerId(pointerIndex)

        var lastX = state.lastX
        var lastY = state.lastY
        var activePointer = state.activePointer
        var photoState = state.photoState

        if (pointerId == state.activePointer) {
            val newPointerIndex = if (pointerIndex == 0) 1 else 0
            lastX = motionEvent.getX(newPointerIndex)
            lastY = motionEvent.getY(newPointerIndex)
            activePointer = motionEvent.getPointerId(newPointerIndex)
        }

        if (state.drawType == DrawType.PICTURE) {
            photoState = photoState.copy(photoMode = PhotoMode.NONE)
        }

        return state.copy(lastY = lastY, lastX = lastX, activePointer = activePointer, photoState = photoState)
    }

    fun handleMotionCancel(state: HomeState) : HomeState {
        return state.copy(activePointer = INVALID_POINTER)
    }
}