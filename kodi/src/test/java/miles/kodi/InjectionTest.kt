package miles.kodi

import miles.kodi.api.bind
import miles.kodi.api.injection.KodiInjector
import miles.kodi.api.injection.register
import miles.kodi.api.provider
import miles.kodi.api.scoped
import org.junit.Test
import assertk.assert
import assertk.assertions.isEqualTo

/**
 * Created by peelemil on 10/11/17.
 */
class InjectionTest {

    @Test
    fun testBaseInjection() {
        val kodi = Kodi.init {  }

        kodi.scope {
            with(scoped<Injectable>())
            build {
                bind<String>() using provider { "BRO" }
            }
        }

        val injectable = Injectable()

        injectable.inject(kodi)

        assert(injectable.string).isEqualTo("BRO")
    }

    @Test(expected = UninitializedPropertyAccessException::class)
    fun testInjectionWithoutCallingInject() {
        val kodi = Kodi.init {  }

        kodi.scope {
            with(scoped<Injectable>())
            build {
                bind<String>() using provider { "BRO" }
            }
        }

        Injectable().string
    }

    class Injectable {

        private val injector = KodiInjector(scoped<Injectable>())

        val string : String by injector.register()

        fun inject(kodi: Kodi) = injector.inject(kodi)
    }
}