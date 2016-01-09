package milespeele.canvas.dagger;

import javax.inject.Singleton;

import dagger.Component;
import milespeele.canvas.activity.ActivityBase;
import milespeele.canvas.activity.ActivityHome;
import milespeele.canvas.drawing.DrawingCurve;
import milespeele.canvas.fragment.FragmentBase;
import milespeele.canvas.parse.ParseUtils;
import milespeele.canvas.view.ViewFabMenu;

/**
 * Created by milespeele on 7/5/15.
 */
@Singleton
@Component(modules = {ApplicationModule.class})
public interface ApplicationComponent {

    void inject(DrawingCurve drawingCurve);

    void inject(ActivityBase activityBase);

    void inject(ViewFabMenu viewFabMenu);

    void inject(FragmentBase fragment);
}
