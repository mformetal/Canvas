package miles.kodi

import miles.kodi.api.HasKodi
import miles.kodi.api.inject
import org.junit.Test

/**
 * Created from mbpeele on 10/8/17.
 */
class InjectionTest {

    @Test
    fun testSimpleInjection() {
        class Dependency
        class Simpleton : HasKodi {
            override val kodi by lazy {
                Kodi.init {  }
            }

            val dependency : Dependency by inject(this)
        }

        Simpleton()
    }
}