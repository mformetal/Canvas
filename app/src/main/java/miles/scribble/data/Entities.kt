package miles.scribble.data

import io.realm.RealmList
import io.realm.RealmObject

/**
 * Created by mbpeele on 7/29/17.
 */
open class Drawing(
        var createdAtMillis : Long = 0L,
        var lastEditedMillis : Long = 0L,
        var bytes: ByteArray ?= null) : RealmObject()