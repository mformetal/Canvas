package miles.kodi

import assertk.assert
import assertk.assertions.isEqualTo
import assertk.assertions.isNotEqualTo
import miles.kodi.module.Module
import org.junit.Test
import java.util.*

/**
 * Created from mbpeele on 10/7/17.
 */
class ModuleTest {

    @Test
    fun testRetrievingDependency() {
        val module = Module().apply {
            bind<String>() from provider { "bro" }
        }

        val dependency = module.get<String>()
        assert(dependency).isEqualTo("bro")
    }

    @Test(expected = IllegalStateException::class)
    fun testBindingSameDependencyClass() {
        Module().apply {
            bind<Thing>() from provider { Thing() }
            bind<Thing>() from provider { Thing() }
        }
    }

    @Test
    fun testRetrievingNewInstanceProvider() {
        val module = Module().apply {
            bind<Thing>() from provider { Thing() }
        }

        val first = module.get<Thing>()
        val second = module.get<Thing>()
        assert(first).isNotEqualTo(second)
    }

    @Test
    fun testRetrievingFromSingletonProvider() {
        val module = Module().apply {
            bind<Thing>() from singleton { Thing(string = UUID.randomUUID().toString()) }
        }

        val first = module.get<Thing>()
        val second = module.get<Thing>()
        assert(first).isEqualTo(second)
    }

    @Test
    fun testAddingChildModules() {
        val root = Module().apply {
            submodule {
                bind<Int>() from provider { 5 }
            }
        }

        val dependency = root.get<Int>()
        assert(dependency).isEqualTo(5)
    }

    @Test
    fun testFactoryCreation() {
        val module = Module().apply {
            bind<String>() from provider { "bro" }
            bind<Int>() from provider { 5 }
            bind<Thing>() from provider { Thing(get(), get()) }
        }

        val thing = module.get<Thing>()
        assert(thing.string).isEqualTo("bro")
        assert(thing.int).isEqualTo(5)
    }

    @Test(expected = NullPointerException::class)
    fun testFactoryCreationWithoutNecessaryDependencies() {
        val module = Module().apply {
            bind<String>() from provider { "bro" }
            bind<Thing>() from provider { Thing(get(), get()) }
        }

        module.get<Thing>()
    }

    @Test
    fun testFactoryCreationWithTags() {
        val module = Module().apply {
            bind<String>("first") from provider { "bro" }
            bind<Int>() from provider { 5 }
            bind<Thing>() from provider { Thing(get("first"), get()) }
        }

        val thing = module.get<Thing>()
        assert(thing.string).isEqualTo("bro")
        assert(thing.int).isEqualTo(5)
    }

    class Thing(val string: String = "", val int: Int = 0)
}