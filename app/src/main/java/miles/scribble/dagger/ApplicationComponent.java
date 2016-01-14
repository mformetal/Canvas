package miles.scribble.dagger;

import javax.inject.Singleton;

import dagger.Component;
import miles.scribble.ui.activity.BaseActivity;
import miles.scribble.ui.drawing.DrawingCurve;
import miles.scribble.ui.fragment.BaseFragment;
import miles.scribble.ui.widget.CanvasLayout;
import miles.scribble.ui.widget.CircleFabMenu;


/**
 * Created by milespeele on 7/5/15.
 */
@Singleton
@Component(modules = {ApplicationModule.class})
public interface ApplicationComponent {

    void inject(DrawingCurve drawingCurve);

    void inject(BaseActivity baseActivity);

    void inject(CircleFabMenu viewFabMenu);

    void inject(CanvasLayout canvasLayout);

    void inject(BaseFragment fragment);
}
