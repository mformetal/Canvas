package miles.scribble.ui

import android.os.Bundle
import android.support.v4.app.DialogFragment
import miles.kodi.Kodi
import miles.kodi.api.Delinker
import miles.scribble.util.extensions.app

/**
 * Created by mbpeele on 10/8/17.
 */
abstract class KodiDialogFragment : DialogFragment() {

    var delinker : Delinker?= null

    override fun onCreate(savedInstanceState: Bundle?) {
        installModule(activity.app.kodi)
        super.onCreate(savedInstanceState)
    }

    override fun onDestroy() {
        super.onDestroy()

        delinker?.delink()
        delinker = null
    }

    abstract fun installModule(kodi: Kodi) : Delinker

}