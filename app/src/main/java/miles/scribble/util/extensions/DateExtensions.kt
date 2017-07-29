package miles.scribble.util.extensions

import org.threeten.bp.Instant
import org.threeten.bp.ZoneOffset

/**
 * Created by mbpeele on 7/29/17.
 */
object DateExtensions {

    val currentOffset : ZoneOffset
        get() = ZoneOffset.systemDefault().rules.getOffset(Instant.now())

    val currentTimeInMillis : Long
        get() = Instant.now().toEpochMilli()

}