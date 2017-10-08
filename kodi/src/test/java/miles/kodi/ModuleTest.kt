package miles.kodi

import assertk.assert
import assertk.assertions.isEqualTo
import assertk.assertions.isNotEqualTo
import miles.kodi.module.factory
import miles.kodi.module.module
import miles.kodi.module.provider
import miles.kodi.module.singleton
import org.junit.Test
import java.util.*

/**
 * Created by mbpeele on 10/7/17.
 */
class ModuleTest {

    @Test
    fun testRetrievingDependency() {
        val module = module {
            bind<String>() by provider { "bro" }
        }

        val dependency = module.get<String>()
        assert(dependency).isEqualTo("bro")
    }

    @Test(expected = IllegalStateException::class)
    fun testBindingSameDependencyClass() {
        module {
            bind<String>() by provider { "bro" }
            bind<String>() by provider { "this" }
        }
    }

    @Test
    fun testRetrievingNewInstanceProvider() {
        val module = module {
            bind<Thing>() by provider { Thing() }
        }

        val first = module.get<Thing>()
        val second = module.get<Thing>()
        assert(first).isNotEqualTo(second)
    }

    @Test
    fun testRetrievingFromSingletonProvider() {
        val module = module {
            bind<Thing>() by singleton { Thing(string = UUID.randomUUID().toString()) }
        }

        val first = module.get<Thing>()
        val second = module.get<Thing>()
        assert(first).isEqualTo(second)
    }

    @Test
    fun testAddingChildModules() {
        val root = module {
            submodule(module {
                bind<Int>() by provider { 5 }
            })
        }

        val dependency = root.get<Int>()
        assert(dependency).isEqualTo(5)
    }

    @Test
    fun testFactoryCreation() {
        val module = module {
            bind<String>() by provider { "bro" }
            bind<Int>() by provider { 5 }
            bind<Thing>() by factory { Thing(get(), get()) }
        }

        val thing = module.get<Thing>()
        assert(thing.string).isEqualTo("bro")
        assert(thing.int).isEqualTo(5)
    }

    @Test(expected = IllegalStateException::class)
    fun testFactoryCreationWithoutNecessaryDependencies() {
        val module = module {
            bind<String>() by provider { "bro" }
            bind<Thing>() by factory { Thing(get(), get()) }
        }

        module.get<Thing>()
    }

    @Test
    fun testFactoryCreationWithTags() {
        val module = module {
            bind<String>("first") by provider { "bro" }
            bind<Int>() by provider { 5 }
            bind<Thing>() by provider { Thing(get("first"), get()) }
        }

        val thing = module.get<Thing>()
        assert(thing.string).isEqualTo("bro")
        assert(thing.int).isEqualTo(5)
    }

    class Thing(val string: String = "", val int: Int = 0)
}