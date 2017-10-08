package miles.kodi

import assertk.assert
import assertk.assertions.isEqualTo
import assertk.assertions.isNotEqualTo
import org.junit.Test
import java.util.*

/**
 * Created from mbpeele on 10/7/17.
 */
class KodiTest {

    @Test
    fun testLinkingToRootNode() {
        val kodi = Kodi.init {

        }

        kodi.link(Kodi.ROOT, Activity::class, {

        })

        kodi.root.run {
            assert(children.size == 1)
            assert(children[0].parent == this)
        }
    }

    @Test
    fun testLinkingToChildrenOfRoot() {
        val kodi = Kodi.init {  }

        kodi.link(Kodi.ROOT, Activity::class, {

        })

        kodi.link(Activity::class, Fragment::class, {

        })

        val activityNode = kodi.root.children[0]
        val fragmentNode = activityNode.children[0]
        assert(activityNode.children.contains(fragmentNode))
        assert(fragmentNode.parent).isEqualTo(activityNode)
    }

    @Test
    fun testUnbindingNode() {
        val kodi = Kodi.init {  }

        val link = kodi.link(Kodi.ROOT, Activity::class, {

        })

        link.delink()

        assert(kodi.root.children.isEmpty())
    }

    @Test
    fun testDependencyFromRootModule() {
        val kodi = Kodi.init {
            bind<App>() from singleton { App() }
        }

        kodi.link(Kodi.ROOT, Activity::class, {
            bind<AppDependency>() from provider { AppDependency(it.instance()) }
        })

        val first = kodi.instance<AppDependency>()
        val second = kodi.instance<AppDependency>()
        assert(first).isNotEqualTo(second)
        assert(first.app).isEqualTo(second.app)
        assert(first.app.tag).isEqualTo(second.app.tag)
    }

    @Test
    fun testDependencyOnChildModule() {
        val kodi = Kodi.init {
            bind<App>() from singleton { App() }
        }

        kodi.link(Kodi.ROOT, Activity::class, {
            bind<Activity>() from provider { Activity() }
        })

        kodi.link(Activity::class, Fragment::class, {
            bind<FragmentDependency>() from provider { FragmentDependency(it.instance()) }
        })

        val fragmentDependency = kodi.instance<FragmentDependency>()
        assert(fragmentDependency).isNotEqualTo(kodi.instance<FragmentDependency>())
    }

    class App(val tag : String = UUID.randomUUID().toString())
    class Activity(val tag : String = UUID.randomUUID().toString())
    class Fragment(val tag : String = UUID.randomUUID().toString())
    class FragmentDependency(val activity: Activity)
    class AppDependency(val app: App)
}