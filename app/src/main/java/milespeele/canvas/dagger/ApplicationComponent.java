package milespeele.canvas.dagger;

import javax.inject.Singleton;

import dagger.Component;
import milespeele.canvas.activity.ActivityHome;
import milespeele.canvas.fragment.FragmentMasterpiece;
import milespeele.canvas.parse.ParseUtils;

/**
 * Created by milespeele on 7/5/15.
 */
@Singleton
@Component(modules = {ApplicationModule.class})
public interface ApplicationComponent {

    void inject(ParseUtils utils);
    void inject(ActivityHome activityHome);
    void inject(FragmentMasterpiece fragmentMasterpiece);
}
