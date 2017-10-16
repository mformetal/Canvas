package miles.scribble.ui.glide

import android.content.Context
import android.util.Log
import com.bumptech.glide.GlideBuilder
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.module.AppGlideModule

/**
 * Created from mbpeele on 9/10/17.
 */
@GlideModule
class CanvasGlideModule : AppGlideModule() {

    override fun applyOptions(context: Context?, builder: GlideBuilder) {
        builder.setLogLevel(Log.VERBOSE)
    }
}