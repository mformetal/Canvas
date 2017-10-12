package miles.kodi

import miles.kodi.api.bind
import miles.kodi.api.injection.KodiInjector
import miles.kodi.api.injection.register
import miles.kodi.api.provider
import miles.kodi.api.scoped
import org.junit.Test
import assertk.assert
import assertk.assertions.isEqualTo
import miles.kodi.api.injection.InjectionRegistry
import miles.kodi.internal.InjectNotCalledException

/**
 * Created by peelemil on 10/11/17.
 */
class InjectionTest {

    @Test
    fun testBaseInjection() {
        val kodi = Kodi.init {  }

        kodi.scope {
            with(scoped<SimpleInjection>())
            build {
                bind<String>() using provider { "BRO" }
            }
        }

        val injectable = SimpleInjection()

        injectable.inject(kodi)

        assert(injectable.string).isEqualTo("BRO")
    }

    @Test(expected = InjectNotCalledException::class)
    fun testInjectionWithoutCallingInject() {
        val kodi = Kodi.init {  }

        kodi.scope {
            with(scoped<SimpleInjection>())
            build {
                bind<String>() using provider { "BRO" }
            }
        }

        SimpleInjection().string
    }

    class SimpleInjection {

        private val injector : InjectionRegistry = KodiInjector(scoped<SimpleInjection>())

        val string: String by injector.register()

        fun inject(kodi: Kodi) = injector.inject(kodi, )
    }
}