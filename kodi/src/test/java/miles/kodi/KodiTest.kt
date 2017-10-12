package miles.kodi

import org.junit.Test
import assertk.assert
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import miles.kodi.api.*
import miles.kodi.internal.key
import java.util.*

/**
 * Created by peelemil on 10/11/17.
 */
class KodiTest {

    @Test
    fun testInitializingKodi() {
        val kodi = Kodi.init {

        }

        assert(kodi.root.parent == null)
        assert(kodi.root.scope == Kodi.ROOT_SCOPE)
        assert(kodi.root.module.providers.isEmpty())
        assert(kodi.root.children.isEmpty())
    }

    @Test
    fun testTreeStructureWhenScopingOnRoot() {
        val kodi = Kodi.init {

        }

        kodi.scope {
            with(scoped<Activity>())
            build { }
        }

        assert(kodi.root.children.size).isEqualTo(1)
        kodi.root.children.first().run {
            assert(module.providers.size).isEqualTo(0)
            assert(parent).isEqualTo(kodi.root)
            assert(scope).isEqualTo(scoped<Activity>())
        }
    }

    @Test
    fun testRetrievingDependenciesFromRoot() {
        val kodi = Kodi.init {
            bind<DependencyOne>() using provider { DependencyOne() }
            bind<SmallThing>() using provider { SmallThing(get()) }
        }

        kodi.root.module.providers.run {
            val key = SmallThing::class.simpleName
            val provider = get(key)
            assert(provider).isNotNull()
            val smallThing = provider!!.provide()
            kotlin.assert(smallThing is SmallThing)
        }
    }

    @Test
    fun testChildNodeRetrievingDependencies() {
        val kodi = Kodi.init {
            bind<DependencyOne>() using provider { DependencyOne() }
            bind<SmallThing>() using provider { SmallThing(get()) }
        }

        kodi.scope {
            with(scoped<Activity>())
            build {
                bind<DependencyTwo>() using provider { DependencyTwo() }
                bind<MediumThing>() using provider { MediumThing(get(), get()) }
            }
        }

        kodi.root.search { it.scope == scoped<Activity>() }!!
                .run {
                    val key = MediumThing::class.simpleName
                    val provider = module.providers[key]
                    assert(provider).isNotNull()
                    val mediumThing = provider!!.provide()
                    kotlin.assert(mediumThing is MediumThing)
                }
    }

    @Test(expected = IllegalStateException::class)
    fun testRetrievingDependenciesOfDifferentScope() {
        class First
        class Second

        val kodi = Kodi.init {
            bind<DependencyOne>() using provider { DependencyOne() }
            bind<SmallThing>() using provider { SmallThing(get()) }
        }

        kodi.scope {
            with(scoped<First>())
            build {
                bind<DependencyTwo>() using provider { DependencyTwo() }
            }
        }

        kodi.scope {
            with(scoped<Second>())
            build {
                bind<MediumThing>() using provider { MediumThing(get(), get()) }
            }
        }

        kodi.root.search { it.scope == scoped<Second>() }!!
                .run {
                    module.providers[MediumThing::class.simpleName]!!.provide()
                }
    }

    @Test
    fun testChildNodeRetrievingSingletonDependencies() {
        val kodi = Kodi.init {
            bind<DependencyOne>() using singleton { DependencyOne() }
            bind<SmallThing>() using provider { SmallThing(get()) }
        }

        kodi.scope {
            with(scoped<Activity>())
            build {
                bind<DependencyTwo>() using provider { DependencyTwo() }
                bind<MediumThing>() using provider { MediumThing(get(), get()) }
            }
        }

        kodi.root.search { it.scope == scoped<Activity>() }!!
                .run {
                    val key = MediumThing::class.simpleName
                    val provider = module.providers[key]
                    assert(provider).isNotNull()
                    val mediumThing = provider!!.provide() as MediumThing
                    val mediumThingDependencyOne = mediumThing.dependencyOne
                    val rootDependencyOne = kodi.root.module.providers[DependencyOne::class.simpleName]!!.provide() as DependencyOne
                    assert(mediumThingDependencyOne).isEqualTo(rootDependencyOne)
                    assert(mediumThingDependencyOne.id).isEqualTo(rootDependencyOne.id)
                }
    }

    @Test
    fun testUnregisteringRemovesNodeFromTree() {
        val kodi = Kodi.init {
            bind<DependencyOne>() using singleton { DependencyOne() }
            bind<SmallThing>() using provider { SmallThing(get()) }
        }

        val registry = kodi.scope {
            with(scoped<Activity>())
            build {
                bind<DependencyTwo>() using provider { DependencyTwo() }
                bind<MediumThing>() using provider { MediumThing(get(), get()) }
            }
        }

        registry.unregister()

        val result = kodi.root.search { it.scope == scoped<Activity>() }
        assert(result).isNull()
    }

    @Test
    fun testBindingSameClassInSameScopeInDifferentPlacesWithoutTag() {
        val first = "1"
        val second = "2"

        val kodi = Kodi.init {
            bind<DependencyOne>() using provider { DependencyOne(first) }
        }

        kodi.scope {
            with(scoped<Activity>())
            build {
                bind<DependencyOne>() using provider { DependencyOne(second) }
            }
        }

        val instance = kodi.get<DependencyOne>(scoped<Activity>())
        assert(instance.id).isEqualTo(second)
    }

    @Test
    fun testBindingSameClassInSameScopeInDifferentPlacesWithTag() {
        val first = "1"
        val second = "2"

        val kodi = Kodi.init {
            bind<DependencyOne>("app") using provider { DependencyOne(first) }
        }

        kodi.scope {
            with(scoped<Activity>())
            build {
                bind<DependencyOne>() using provider { DependencyOne(second) }
            }
        }

        val instance = kodi.get<DependencyOne>(scoped<Activity>(), "app")
        assert(instance.id).isEqualTo(first)
    }

    class Activity

    class DependencyOne(val id: String = UUID.randomUUID().toString())
    class DependencyTwo
    class DependencyThree
    class SmallThing(val dependencyOne: DependencyOne)
    class MediumThing(val dependencyOne: DependencyOne, val dependencyTwo: DependencyTwo)
    class HugeThing(val dependencyOne: DependencyOne, val dependencyTwo: DependencyTwo, val dependencyThree: DependencyThree)
}