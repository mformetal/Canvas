package miles.kodi.api

/**
 * Created by mbpeele on 10/7/17.
 */
interface Delinker {

    fun delink()

    companion object {
        val EMPTY = object : Delinker {
            override fun delink() {

            }
        }
    }

}