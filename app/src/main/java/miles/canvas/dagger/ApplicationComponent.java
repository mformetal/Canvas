package miles.canvas.dagger;

import javax.inject.Singleton;

import dagger.Component;
import miles.canvas.activity.ActivityBase;
import miles.canvas.drawing.DrawingCurve;
import miles.canvas.fragment.FragmentBase;
import miles.canvas.view.ViewFabMenu;

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
