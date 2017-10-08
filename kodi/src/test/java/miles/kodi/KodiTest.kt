package miles.kodi

import assertk.assert
import assertk.assertions.isEqualTo
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

            link(Kodi.ROOT, Activity::class, module {

            })
        }

        kodi.root.run {
            assert(children.size == 1)
            assert(children[0].parent == kodi.root)
        }
    }

    @Test
    fun testLinkingToChildrenOfRoot() {
        val kodi = kodi {
            root {

            }

            link(Kodi.ROOT, Activity::class, module {

            })

            link(Activity::class, Fragment::class, module {

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

            link(Kodi.ROOT, Activity::class, module {

            })
        }

        val link = kodi.link(Activity::class, Fragment::class, module {

        })

        link.unbind()

        val activityNode = kodi.root.children[0]
        assert(activityNode.children.isEmpty())
    }

    @Test
    fun testDependencyFromRootModule() {
        val kodi = kodi {
            root {
                bind<App>() by singleton { App() }
            }

            link(Kodi.ROOT, Activity::class, module {
                bind<AppDependency>() by factory { AppDependency(instance()) }
            })
        }

        val first = kodi.instance<AppDependency>()
        val second = kodi.instance<AppDependency>()
        assert(first.app).isEqualTo(second.app)
    }

    class App(val tag : String = UUID.randomUUID().toString())
    class Activity(val tag : String = UUID.randomUUID().toString())
    class Fragment(val tag : String = UUID.randomUUID().toString())
    class AppDependency(val app: App)
}