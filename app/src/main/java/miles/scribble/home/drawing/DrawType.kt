package miles.scribble.home.drawing

import android.net.Uri

/**
 * Created by mbpeele on 8/12/17.
 */
sealed class DrawType {
    class Normal : DrawType()
    class Ink : DrawType()
    class Erase : DrawType()
    class Picture(val uri: Uri) : DrawType()
}