package miles.scribble.home.viewmodel

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import miles.scribble.App
import miles.scribble.util.extensions.getDisplaySize
import java.io.File
import java.util.concurrent.Executors

class HomeViewModelFactory(private val app: App) : ViewModelProvider.Factory {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        val cachedDrawingFile = File("${app.filesDir}/$CACHED_DRAWING_NAME")
        val bitmap = if (cachedDrawingFile.exists()) {
            app.openFileInput(CACHED_DRAWING_NAME)
                    .use {
                        val options = BitmapFactory.Options().apply {
                            inPreferredConfig = Bitmap.Config.ARGB_8888
                            inMutable = true
                        }
                        BitmapFactory.decodeStream(it, null, options)
                    }
        } else {
            val displaySize = app.getDisplaySize()
            Bitmap.createBitmap(displaySize.x, displaySize.y, Bitmap.Config.ARGB_8888).apply {
                eraseColor(Color.WHITE)
            }
        }

        @Suppress("UNCHECKED_CAST")
        return HomeViewModel(HomeStore(bitmap), app, Executors.newSingleThreadExecutor()) as T
    }
}