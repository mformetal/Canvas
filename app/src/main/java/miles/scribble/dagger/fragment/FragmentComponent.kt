package miles.scribble.dagger.fragment

import android.app.Activity
import android.support.v4.app.Fragment
import dagger.MembersInjector

interface FragmentComponent<F : Fragment> : MembersInjector<F>