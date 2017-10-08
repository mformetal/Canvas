package miles.kodi

import assertk.assert
import assertk.assertions.isEqualTo
import assertk.assertions.isNotEqualTo
import miles.kodi.module.factory
import miles.kodi.module.module
import org.junit.Test
import java.util.*

/**
 * Created by mbpeele on 10/7/17.
 */
class KodiTest {

    @Test
    fun testRootNode() {
        val rootModule = module {

        }
        val kodi = kodi {
            root(rootModule)
        }

        assert(kodi.root.parent == null)
        assert(kodi.root.children.isEmpty())
        assert(kodi.root.module == rootModule)
    }

    @Test
    fun testLinkingToRootNode() {
        val kodi = kodi {
            root {

            }

            link(Kodi.ROOT, Activity::class, {

            })
        }

        kodi.root.run {
            assert(children.size == 1)
            assert(children[0].parent == this)
        }
    }

    @Test
    fun testImplicitLinking() {
        class Test {
            val kodi = miles.kodi.kodi {
                root {

                }

                link(Kodi.ROOT, {  })
            }
        }

        val test = Test()
        test.kodi.root.run {
            assert(children.size == 1)
            assert(children[0].parent == this)
            assert(children[0].scope == Test::class)
        }
    }

    @Test
    fun testLinkingToChildrenOfRoot() {
        val kodi = kodi {
            root {

            }

            link(Kodi.ROOT, Activity::class, {

            })

            link(Activity::class, Fragment::class, {

            })
        }

        val activityNode = kodi.root.children[0]
        val fragmentNode = activityNode.children[0]
        assert(activityNode.children.contains(fragmentNode))
        assert(fragmentNode.parent).isEqualTo(activityNode)
    }

    @Test
    fun testUnbindingNode() {
        val kodi = kodi {
            root {

            }

            link(Kodi.ROOT, Activity::class, {

            })
        }

        val link = kodi.link(Activity::class, Fragment::class, {

        })

        link.delink()

        val activityNode = kodi.root.children[0]
        assert(activityNode.children.isEmpty())
    }

    @Test
    fun testDependencyFromRootModule() {
        val kodi = kodi {
            root {
                bind<App>() from singleton { App() }
            }

            link(Kodi.ROOT, Activity::class, {
                bind<AppDependency>() from factory { AppDependency(instance()) }
            })
        }

        val first = kodi.instance<AppDependency>()
        val second = kodi.instance<AppDependency>()
        assert(first).isNotEqualTo(second)
        assert(first.app).isEqualTo(second.app)
        assert(first.app.tag).isEqualTo(second.app.tag)
    }

    @Test
    fun testDependencyOnChildModule() {
        val kodi = kodi {
            root {
                bind<App>() from singleton { App() }
            }

            link(Kodi.ROOT, Activity::class, {
                bind<Activity>() from factory { Activity() }
            })

            link(Activity::class, Fragment::class, {
                bind<FragmentDependency>() from factory { FragmentDependency(instance()) }
            })
        }

        val fragmentDependency = kodi.instance<FragmentDependency>()
        assert(fragmentDependency).isNotEqualTo(kodi.instance<FragmentDependency>())
    }

    class App(val tag : String = UUID.randomUUID().toString())
    class Activity(val tag : String = UUID.randomUUID().toString())
    class Fragment(val tag : String = UUID.randomUUID().toString())
    class FragmentDependency(val activity: Activity)
    class AppDependency(val app: App)
}