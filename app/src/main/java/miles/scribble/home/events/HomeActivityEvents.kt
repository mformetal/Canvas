package miles.scribble.home.events

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import miles.scribble.home.drawing.DrawType
import miles.scribble.home.viewmodel.HomeState
import miles.redux.core.Event
import miles.redux.core.Reducer
import miles.scribble.util.extensions.identity


/**
 * Created using mbpeele on 8/12/17.
 */
sealed class HomeActivityEvents : Event {
    class PictureChosen(val contentResolver: ContentResolver, val uri: Uri) : HomeActivityEvents()
}

class HomeActivityEventsReducer : Reducer<HomeActivityEvents, HomeState> {

    override fun reduce(event: HomeActivityEvents, state: HomeState): HomeState {
        return when (event) {
            is HomeActivityEvents.PictureChosen -> {
                val photoBitmapState = state.photoState

                val inputStream = event.contentResolver.openInputStream(event.uri)
                val options = BitmapFactory.Options().apply {
                    inPreferredConfig = Bitmap.Config.ARGB_8888
                    inMutable = true
                }
                val bitmap = BitmapFactory.decodeStream(inputStream, null, options)
                val photoBitmapMatrix = Matrix(photoBitmapState.matrix).identity()
                inputStream.close()

                var scale = Math.min(state.bitmap.width.toFloat() / bitmap.width,
                        state.bitmap.height.toFloat() / bitmap.height)
                scale = Math.max(scale, Math.min(state.bitmap.height.toFloat() / bitmap.width,
                        state.bitmap.width.toFloat() / bitmap.height))
                if (scale < 1) {
                    photoBitmapMatrix.setScale(scale, scale)
                }

                state.copy(drawType = DrawType.PICTURE,
                        photoState = photoBitmapState.copy(matrix = photoBitmapMatrix, photoBitmap = bitmap),
                        isMenuOpen = false)
            }
        }
    }
}